package elite.kit.outwait.remoteDataSource

import android.util.Log
import elite.kit.outwait.clientDatabase.ClientInfo
import elite.kit.outwait.clientDatabase.ClientInfoDao
import elite.kit.outwait.networkProtocol.*

class SocketIOClientHandler(private val dao: ClientInfoDao) : ClientHandler {

    private val namespaceClient: String = "/client"

    /*
    HashMap mapped Events und passende Callbacks der ClientHandler Instanz
     */
    private val clientEventToCallbackMapping: HashMap<Event,
            (wrappedJSONData: JSONObjectWrapper) -> Unit> = hashMapOf()

    private var serverReady = false

    private val cSocket: SocketAdapter


    init {
        cSocket = SocketAdapter(namespaceClient)

        // configure HashMap that maps receiving events to callbacks
        clientEventToCallbackMapping[Event.SEND_SLOT_DATA] = { receivedData ->
            onSendSlotData(receivedData as JSONSlotDataWrapper)
        }
        clientEventToCallbackMapping[Event.READY_TO_SERVE] = { receivedData ->
            onReadyToServe(receivedData as JSONEmptyWrapper)
        }
        clientEventToCallbackMapping[Event.END_SLOT] = { receivedData ->
            onEndSlot(receivedData as JSONSlotCodeWrapper)
        }
        clientEventToCallbackMapping[Event.DELETE_SLOT] = { receivedData ->
            onDeleteSlot(receivedData as JSONSlotCodeWrapper)
        }
        clientEventToCallbackMapping[Event.INVALID_CODE] = { receivedData ->
            onInvalidCode(receivedData as JSONEmptyWrapper)
        }
        clientEventToCallbackMapping[Event.INVALID_REQUEST] = { receivedData ->
            onInvalidRequest(receivedData as JSONInvalidRequestWrapper)
        }
    }

    override fun initCommunication(): Boolean {
        Log.d("initCom::SIOCliHandler", "reached")

        cSocket.initializeConnection(clientEventToCallbackMapping)

        // Mit return warten bis SocketIOSocket connected ist (TODO geht auch schöner? LiveData?)
        while (cSocket.isConnected() == false) Thread.sleep(1000)

        // Mit return warten bis Server readyToServe signalisiert (TODO geht auch schöner? LiveData?)
        while (!this.serverReady) Thread.sleep(1000)

        return true
    }

    override fun endCommunication(): Boolean {
        cSocket.releaseConnection()
        this.serverReady = false
        return true
    }

    override fun newCodeEntered(slotCode: String) {
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

        // check if clientInfo existed already and has to be updated or inserted for the first time
        // TODO will getClientInfo always return not null ? (see gitlab issues)
        if (dao.getClientInfo(slotCode) != null) {

            // get originalAppointmentTime of existing clientInfo object
            val originalAppointmentTime = dao.getClientInfo(slotCode).originalAppointmentTime
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

        // delete ClientInfo from ClientDB
        dao.deleteClientInfo(endedClientInfo)
    }

    private fun onDeleteSlot(wrappedJSONData: JSONSlotCodeWrapper) {
        val deletedSlotCode = wrappedJSONData.getSlotCode()
        val deletedClientInfo = dao.getClientInfo(deletedSlotCode)

        // delete ClientInfo from ClientDB
        dao.deleteClientInfo(deletedClientInfo)
    }

    private fun onInvalidCode(wrappedJSONData: JSONEmptyWrapper) {
        //TODO 1 Fehlermeldung oder LiveData um Repo zu benachrichtigen?
        // -> mit Benni abklären
        // TODO 2 Soll nochmal der invalide Code vom Server geschickt werden?
    }

    private fun onInvalidRequest(wrappedJSONData: JSONInvalidRequestWrapper) {
        val errorMessage = wrappedJSONData.getErrorMessage()
        //TODO Fehlermeldung werfen
    }

}
