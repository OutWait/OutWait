package elite.kit.outwait.remoteDataSource

import android.util.Log
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
        // TODO Mit SEND_SLOT_APPROX zusammenfassen (Siehe issue auf gitlab)
       /*
        clientEventToCallbackMapping[Event.UPDATE_MANAGEMENT_INFORMATION] = { receivedData ->
            onUpdateManagementInformation(receivedData as JSONUpdateManagementInformationWrapper)
        }
        clientEventToCallbackMapping[Event.SEND_SLOT_APPROX] = { receivedData ->
            onSendSlotApprox(receivedData as JSONSlotApproxWrapper)
        }

        */

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


    //TODO Hier solang mit Rückgabe warten bis Server "readyToServe" geschickt hat (Zustandsvariable)
    override fun initCommunication(): Boolean {

        Log.d("initCom::SIOCliHandler", "reached")
        cSocket.initializeConnection(clientEventToCallbackMapping)

        // Mit return warten bis SocketIOSocket connected ist
        while (cSocket.isConnected() == false) Thread.sleep(1000)

        return true
    }

    override fun endCommunication(): Boolean {
        cSocket.releaseConnection()
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

    //TODO Mit onSendSloxApprox zusammenfassen (siehe gitlab issue)
    private fun onUpdateManagementInformation(wrappedJSONData: JSONUpdateManagementInformationWrapper) {
        val slotCode = wrappedJSONData.getSlotCode()
        val notificationTime = wrappedJSONData.getNotificationTime()
        val delayNotificationTime = wrappedJSONData.getDelayNotificationTime()
        val name = wrappedJSONData.getName()
    }
    //TODO Mit onUpdateManagementInformation zusammenfassen (siehe gitlab issue)
    private fun onSendSlotApprox(wrappedJSONData: JSONSlotApproxWrapper) {
        val slotCode = wrappedJSONData.getSlotCode()
        val approxTime = wrappedJSONData.getApproxTime()
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
