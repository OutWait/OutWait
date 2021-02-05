package elite.kit.outwait.remoteDataSource

import io.socket.client.IO
import io.socket.client.Socket
import java.net.URI

//TODO Fehler werfen bei Verbindungsfehler/Abbruch (inkl. der Listener)

class SocketAdapter(private val serverURI: String) {

    private val mSocket: Socket? = null

    init {
        val options = IO.Options()
        options.reconnection = true

        val mSocket = IO.socket(URI.create(serverURI), options)
    }

    fun initializeConnection() {
        //TODO Param für EventListener

        registerEventListeners()
        mSocket?.connect()

        //TODO Was ist mit on("Connect") Event?
    }

    fun emitEventToServer() {
        // TODO: implementen
        // TODO param ist EventString und JSONObject
        //TODO hier logik um JSONObj. in JSON String umzuwandeln
    }


    private fun registerEventListeners() {
        //TODO: Wie werden die/der Callback übergeben mit den Events?
        //TODO: Ein Callback und Strategie im konkreten Handler

        //TODO: JSON-String in JSON-Object Konvertierung auslagern?
    }

    fun releaseConnection() {
        //TODO Was noch um alle Ressourcen freizugeben?
        mSocket?.close()
    }

}
