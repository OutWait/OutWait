package elite.kit.outwait.remoteDataSource

import org.json.JSONObject

const val namespaceClient: String = "/client"

class SocketIOClientHandler : ClientHandler {

    private val cSocket: SocketAdapter



    init {
        cSocket = SocketAdapter(namespaceClient)
    }

    //TODO Mit ObjectWrappern die Daten zum versenden verpacken
    //TODO Mit Strategie (oder internen Methoden, da net so viele) die incomingEvents verarbeiten
    //Falls nur interne Methoden, dann diese direkt in Event-Callback-Mapping einfügen?

    override fun initCommunication(): Boolean {
        TODO("Not yet implemented")
    }

    override fun endCommunication(): Boolean {
        TODO("Not yet implemented")
    }

    override fun newCodeEntered(slotCode: String) {
        var data: JSONObject = JSONObject()
        data.put("slotCode", slotCode)
        cSocket.emitEventToServer(ClientEvents.LISTEN_SLOT.getEventString(), data)
    }

    override fun refreshWaitingTime(slotCode: String) {
        var data: JSONObject = JSONObject()
        data.put("slotCode", slotCode)
        cSocket.emitEventToServer(ClientEvents.REFRESH_SLOT_APPROX.getEventString())
    }

    private fun processIncomingEvent(event: String, data: JSONObject) {

        //TODO Strategie verwenden um Daten zu verarbeiten

    }

}
