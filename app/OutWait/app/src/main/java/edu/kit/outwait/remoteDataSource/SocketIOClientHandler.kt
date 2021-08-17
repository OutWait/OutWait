package edu.kit.outwait.remoteDataSource

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import edu.kit.outwait.clientDatabase.ClientInfo
import edu.kit.outwait.clientDatabase.ClientInfoDao
import edu.kit.outwait.networkProtocol.*

/**
 * Defines the maximum amount of time waited for an awaited response of the server
 */
private const val MAX_TIME_WAIT_FOR_RESPONSE = 3000L

/**
 * Defines the sampling granulation with which received server response is checked
 */
private const val TIME_STEP_FOR_RESPONSE_WAIT = 100L

/**
 * The namespace for the client connection
 */
private const val namespaceClient: String = "/client"

/**
 * This class represents the "concrete product" of ClientHandler
 * in the, here used and commonly known as, "abstract factory pattern".
 * It implements all methods for network communication, that the
 * client repository (or higher tier) can use to send to and receive data from the server,
 * using the implementation of a web socket connection.
 *
 * @property dao of type ClientInfoDao, used to inject the client database (or rather access to it)
 * into the ClientHandler using so called DependencyInjection
 */
class SocketIOClientHandler(private val dao: ClientInfoDao) : ClientHandler {

    /**
     * LiveData to provide the client repository with useful error messages,
     * backed by private property
     */
    private val _errors = MutableLiveData<List<ClientServerErrors>>()
    override fun getErrors() = _errors as LiveData<List<ClientServerErrors>>

    /**
     *  Mapping of events and their callbacks, used to initialize the SocketAdapter
     */
    private val clientEventToCallbackMapping: HashMap<Event,
            (wrappedJSONData: JSONObjectWrapper) -> Unit> = hashMapOf()

    /**
     * State variable, indicating if the ClientHandler can send further events to the server
     */
    private var serverReady = false

    /** The underlying socket adapter, facade for the websocket */
    private val cSocket: SocketAdapter


    init {
        cSocket = SocketAdapter(namespaceClient)

        // configure HashMap that maps receiving events to callbacks
        clientEventToCallbackMapping[Event.SEND_SLOT_DATA_C] = { receivedData ->
            onSendSlotData(receivedData as JSONSlotDataWrapper)
        }
        clientEventToCallbackMapping[Event.READY_TO_SERVE_C] = { receivedData ->
            onReadyToServe(receivedData as JSONEmptyWrapper)
        }
        clientEventToCallbackMapping[Event.END_SLOT_C] = { receivedData ->
            onEndSlot(receivedData as JSONSlotCodeWrapper)
        }
        clientEventToCallbackMapping[Event.DELETE_SLOT_C] = { receivedData ->
            onDeleteSlot(receivedData as JSONSlotCodeWrapper)
        }
        clientEventToCallbackMapping[Event.INVALID_CODE_C] = { receivedData ->
            onInvalidCode(receivedData as JSONSlotCodeWrapper)
        }
        clientEventToCallbackMapping[Event.INVALID_REQUEST_C] = { receivedData ->
            onInvalidRequest(receivedData as JSONErrorMessageWrapper)
        }
        clientEventToCallbackMapping[Event.NETWORK_ERROR] = { receivedData ->
            onNetworkError(receivedData as JSONEmptyWrapper)
        }
    }

    /**
     * This method initializes the communication with the server by initializing the underlying
     * socket adapter and waiting for the server to respond with "readyToServer@C" indicating
     * further communication is possible
     *
     * @return True if communication was successfully established, returns false else or if
     * the server did not respond before time out
     */
    override fun initCommunication(): Boolean {
        if (!cSocket.initializeConnection(clientEventToCallbackMapping)) {
            pushError(ClientServerErrors.COULD_NOT_CONNECT)
            endCommunication()
            return false
        }

        // wait until server responds with "readyToServer@C"
        var curWaitTimeForResponse = 0L
        while (!this.serverReady and (curWaitTimeForResponse < MAX_TIME_WAIT_FOR_RESPONSE)) {
            curWaitTimeForResponse += TIME_STEP_FOR_RESPONSE_WAIT
            Thread.sleep(TIME_STEP_FOR_RESPONSE_WAIT)
        }
        if (this.serverReady) {
            return true
        } else {
            Log.i("SocketCHandler",
                "ReadyToServe not received since $curWaitTimeForResponse millis")
            pushError(ClientServerErrors.SERVER_DID_NOT_RESPOND)
            endCommunication()
        }
        return false
    }

    /**
     * This method ends the communication with the server, returning after
     * releasing the connection resources of the underlying socket and
     * resetting the state of the ClientHandler
     *
     * @return True, after the connection was successfully released
     */
    override fun endCommunication(): Boolean {
        cSocket.releaseConnection()
        this.serverReady = false
        return true
    }

    /**
     * This method emits the "newCodeEntered@S" event to the server,
     * with the entered slot code as data (wrapped in JSONSlotCodeWrapper)
     *
     * @param slotCode as String, that is to be registered or rather observed
     */
    override fun newCodeEntered(slotCode: String) {
        val event: Event = Event.LISTEN_SLOT
        val data: JSONObjectWrapper = JSONSlotCodeWrapper(slotCode)
        cSocket.emitEventToServer(event.getEventString(), data)
    }

    /**
     * This method emits the "refreshSlotApprox@S" event to the server,
     * with the slot code as data (wrapped in JSONSlotCodeWrapper)
     *
     * @param slotCode as String, for which the refresh was requested
     */
    override fun refreshWaitingTime(slotCode: String) {
        val event: Event = Event.REFRESH_SLOT_APPROX
        val data: JSONObjectWrapper = JSONSlotCodeWrapper(slotCode)
        cSocket.emitEventToServer(event.getEventString(), data)
    }


    /**
     * This callback method is invoked on the "sendSlotData@C" event,
     * constructing a ClientInfo object from the parsed data and insert or update it
     * to the client database
     *
     * @param wrappedJSONData as JSONSlotDataWrapper containing the information to the
     * respective slot
     */
    private fun onSendSlotData(wrappedJSONData: JSONSlotDataWrapper) {
        val slotCode = wrappedJSONData.getSlotCode()
        val approxTime = wrappedJSONData.getApproxTime()
        val instituteName = wrappedJSONData.getInstituteName()
        val notificationTime = wrappedJSONData.getNotificationTime()
        val delayNotificationTime = wrappedJSONData.getDelayNotificationTime()

        // check if clientInfo existed already and has to be updated or else inserted for the first time
        if (dao.getClientInfo(slotCode) != null) {

            // get originalAppointmentTime of existing clientInfo object (not-null assertion)
            val originalAppointmentTime = dao.getClientInfo(slotCode)!!.originalAppointmentTime
            // create new ClientInfo with same originalAppointmentTime as the existing one
            val newClientInfo = ClientInfo(slotCode, instituteName, approxTime, originalAppointmentTime,
                notificationTime, delayNotificationTime)
            dao.update(newClientInfo)
        } else {
            // create new ClientInfo with originalAppointmentTime as the current (and first) approxTime
            val newClientInfo = ClientInfo(slotCode, instituteName, approxTime, approxTime,
                notificationTime, delayNotificationTime)
            dao.insert(newClientInfo)
        }
    }

    /**
     * This callback method is invoked on the "readyToServe@C" event,
     * setting the state of the ClientHandler so that further events to the
     * server can be transmitted
     *
     * @param wrappedJSONData as JSONEmptyWrapper, as no data was transmitted
     */
    private fun onReadyToServe(wrappedJSONData: JSONEmptyWrapper) {
        this.serverReady = true
    }

    /**
     * This callback method is invoked on the "endSlot@C" event,
     * deleting the respective slot from the client database
     *
     * @param wrappedJSONData as JSONSlotCodeWrapper, containing the code
     * of the slot to be deleted
     */
    private fun onEndSlot(wrappedJSONData: JSONSlotCodeWrapper) {
        val endedSlotCode = wrappedJSONData.getSlotCode()
        val endedClientInfo = dao.getClientInfo(endedSlotCode)

        // delete ClientInfo from ClientDB if it exists
        if (endedClientInfo != null) {
            dao.deleteClientInfo(endedClientInfo)
        }
    }

    /**
     * This callback method is invoked on the "deleteSlot@C" event,
     * deleting the respective slot from the client database
     *
     * @param wrappedJSONData as JSONSlotCodeWrapper, containing the code
     * of the slot to be deleted
     */
    private fun onDeleteSlot(wrappedJSONData: JSONSlotCodeWrapper) {
        val deletedSlotCode = wrappedJSONData.getSlotCode()
        val deletedClientInfo = dao.getClientInfo(deletedSlotCode)

        // delete ClientInfo from ClientDB if it exists
        if (deletedClientInfo != null) {
            dao.deleteClientInfo(deletedClientInfo)
        }
    }

    /**
     * This callback method is invoked on the "invalidCode@C" event,
     * pushing the error to the client repository
     *
     * @param wrappedJSONData JSONEmptyWrapper, as no data was transmitted
     */
    private fun onInvalidCode(wrappedJSONData: JSONSlotCodeWrapper) {
        val invalidSlotCode = wrappedJSONData.getSlotCode()
        val expiredClientInfo = dao.getClientInfo(invalidSlotCode)

        // delete expired ClientInfo from ClientDB if it exists and push respective error
        if (expiredClientInfo != null) {
            dao.deleteClientInfo(expiredClientInfo)
            pushError(ClientServerErrors.EXPIRED_SLOT_CODE)
            return
        }

        pushError(ClientServerErrors.INVALID_SLOT_CODE)
    }

    /**
     * This callback method is invoked on the "invalidRequest@C" event,
     * pushing the error to the client repository
     *
     * @param wrappedJSONData JSONErrorMessageWrapper containing the transmitted
     * error message
     */
    private fun onInvalidRequest(wrappedJSONData: JSONErrorMessageWrapper) {
        val errorMessage = wrappedJSONData.getErrorMessage()
        pushError(ClientServerErrors.INVALID_REQUEST)
    }

    /**
     * This callback method is invoked on a network error, when the current connection
     * session is irrevocably lost, so the client repository gets notified
     *
     * @param wrappedJSONData JSONEmptyWrapper, as no data was transmitted
     */
    private fun onNetworkError(wrappedJSONData: JSONEmptyWrapper) {
        pushError(ClientServerErrors.NETWORK_ERROR)
        endCommunication()
    }

    /**
     * This method pushed errors to the client repository via LiveData
     * for useful information about possibly time-displaced error events
     *
     * @param error of type ClientServerErrors, the error to be pushed
     */
    private fun pushError(error: ClientServerErrors){
        Log.i("SocketCHandler", "Error $error was pushed")
        if (_errors.value !== null){
            val newList = _errors.value!!.plus(error).toMutableList()
            _errors.postValue(newList)
        }else{
            _errors.postValue(listOf(error))
        }
    }
}
