package elite.kit.outwait.remoteDataSource

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elite.kit.outwait.clientDatabase.ClientInfo
import elite.kit.outwait.clientDatabase.ClientInfoDao
import elite.kit.outwait.networkProtocol.*

// TODO Falsche Zugriffe durch Repo abfangen (bspw.2x initComm hintereinander)
// TODO InvalidRequest adäquat handlen (oder error pushen?) Welche Fehler sind möglich?
private const val MAX_WAITTIME_FOR_RESPONSE = 10000L
private const val TIME_STEP_FOR_RESPONSE_WAIT = 1000L

class SocketIOClientHandler(private val dao: ClientInfoDao) : ClientHandler {

    private val _errors = MutableLiveData<List<ClientServerErrors>>()
    override fun getErrors() = _errors as LiveData<List<ClientServerErrors>>

    private val namespaceClient: String = "/client"

    /*
    HashMap mapped Events und passende Callbacks der ClientHandler Instanz
     */
    private val clientEventToCallbackMapping: HashMap<Event,
            (wrappedJSONData: JSONObjectWrapper) -> Unit> = hashMapOf()

    private var serverReady = false

    private val cSocket: SocketAdapter

    private var currentSessionID: String = ""


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
            onInvalidCode(receivedData as JSONEmptyWrapper)
        }
        clientEventToCallbackMapping[Event.INVALID_REQUEST_C] = { receivedData ->
            onInvalidRequest(receivedData as JSONErrorMessageWrapper)
        }
    }

    override fun initCommunication(): Boolean {
        Log.d("initCom::SIOCliHandler", "reached")

        if (!cSocket.initializeConnection(clientEventToCallbackMapping)) {
            pushError(ClientServerErrors.COULD_NOT_CONNECT)
            endCommunication()
            return false
        } else {
            this.currentSessionID = cSocket.getCurrentSessionID()
            Log.i("SocketMHandler", "Connection established with $currentSessionID id")
        }

        // Mit return warten bis Server readyToServe signalisiert
        var curWaitTimeForResponse = 0L
        while (!this.serverReady and (curWaitTimeForResponse < MAX_WAITTIME_FOR_RESPONSE)) {
            Log.d("SocketCHandler", "wait for readyToServe since $curWaitTimeForResponse millis")
            curWaitTimeForResponse += TIME_STEP_FOR_RESPONSE_WAIT
            Thread.sleep(TIME_STEP_FOR_RESPONSE_WAIT)
        }
        if (this.serverReady) {
            return true
        } else {
            Log.i("SocketCHandler", "waited in vain for readyToServe since $curWaitTimeForResponse millis")
            pushError(ClientServerErrors.SERVER_DID_NOT_RESPOND)
            endCommunication()
        }
        return false
    }

    override fun endCommunication(): Boolean {
        cSocket.releaseConnection()
        this.serverReady = false
        return true
    }

    override fun newCodeEntered(slotCode: String) {

        Log.d("newC::SIOCliHandler", "newCodeEntered was called")
        val event: Event = Event.LISTEN_SLOT
        val data: JSONObjectWrapper = JSONSlotCodeWrapper(slotCode)

        cSocket.emitEventToServer(event.getEventString(), data)
    }

    override fun refreshWaitingTime(slotCode: String) {
        val event: Event = Event.REFRESH_SLOT_APPROX
        val data: JSONObjectWrapper = JSONSlotCodeWrapper(slotCode)

        cSocket.emitEventToServer(event.getEventString(), data)
    }

    /*
    Die Callback Methoden die gemäß Mapping bei einem eingeheneden Event aufgerufen werden
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
            //  // create new ClientInfo with same originalAppointmentTime as the existing one
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

    /*
    Server erlaubt uns jetzt erst, dass wir weitere Events schicken dürfen
     */
    private fun onReadyToServe(wrappedJSONData: JSONEmptyWrapper) {
        this.serverReady = true
    }

    private fun onEndSlot(wrappedJSONData: JSONSlotCodeWrapper) {
        val endedSlotCode = wrappedJSONData.getSlotCode()
        val endedClientInfo = dao.getClientInfo(endedSlotCode)

        // delete ClientInfo from ClientDB if it exists
        if (endedClientInfo != null) {
            dao.deleteClientInfo(endedClientInfo)
        }
    }

    private fun onDeleteSlot(wrappedJSONData: JSONSlotCodeWrapper) {
        val deletedSlotCode = wrappedJSONData.getSlotCode()
        val deletedClientInfo = dao.getClientInfo(deletedSlotCode)

        // delete ClientInfo from ClientDB if it exists
        if (deletedClientInfo != null) {
            dao.deleteClientInfo(deletedClientInfo)
        }
    }

    private fun onInvalidCode(wrappedJSONData: JSONEmptyWrapper) {
        Log.d("onInvlCd::SIOCliHandler", "server answer")
        pushError(ClientServerErrors.INVALID_SLOT_CODE)
    }

    //TODO Muss und wenn ja, wie errorMessage an Repo hochreichen?
    private fun onInvalidRequest(wrappedJSONData: JSONErrorMessageWrapper) {
        val errorMessage = wrappedJSONData.getErrorMessage()
        pushError(ClientServerErrors.INVALID_REQUEST)
    }

    private fun pushError(error: ClientServerErrors){
        if (_errors.value !== null){
            val newList = _errors.value!!.plus(error).toMutableList()
            _errors.postValue(newList)
        }else{
            _errors.postValue(listOf(error))
        }
    }

}
