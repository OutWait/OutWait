package elite.kit.outwait.remoteDataSource

import android.util.Log
import elite.kit.outwait.clientDatabase.ClientInfoDao
import elite.kit.outwait.networkProtocol.*
import org.json.JSONObject

class SocketIOClientHandler(private val dao: ClientInfoDao) : ClientHandler {

    private val namespaceClient: String = "/client"

    private val cSocket: SocketAdapter



    init {
        cSocket = SocketAdapter(namespaceClient)
    }

    //TODO Mit ObjectWrappern die Daten zum versenden verpacken
    //TODO Mit Strategie (oder internen Methoden, da net so viele) die incomingEvents verarbeiten
    //Falls nur interne Methoden, dann diese direkt in Event-Callback-Mapping einfügen?

    override fun initCommunication(): Boolean {
        Log.d("initCom::SIOCliHandler", "reached")
        return true
        //TODO("Not yet implemented")
    }

    override fun endCommunication(): Boolean {
        TODO("Not yet implemented")
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

    private fun processIncomingEvent(event: Event, wrappedJSONData: JSONObjectWrapper) {

        //TODO Strategie verwenden um Daten zu verarbeiten

    }

}
