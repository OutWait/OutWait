package elite.kit.outwait.remoteDataSource

import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URI


//TODO Fehler werfen bei Verbindungsfehler/Abbruch (inkl. der Listener)

class SocketAdapter(private val serverURI: String) {

    private val mSocket: Socket? = null

    init {
        val options = IO.Options()
        options.reconnection = true

        val mSocket = IO.socket(URI.create(serverURI), options)
    }

    /*
    Intialisiere Verbindung mit Server //TODO Was ist Socket.IO mäßig noch zu beachten?
    Uund registriere die EventListener (in private Methode ausgelagert)
     */
    fun initializeConnection(
        mapEventsToCallback: HashMap<String,
                (event: String, jsonObj: JSONObject) -> Unit>
    ) {
        //TODO Param für EventListener
        registerEventListeners(mapEventsToCallback)
        mSocket?.connect()

        //TODO Was ist mit on("Connect") Event?
    }

    fun emitEventToServer() {
        // TODO: implementen
        // TODO param ist EventString und JSONObject
        //TODO hier logik um JSONObj. in JSON String umzuwandeln
    }


    /*
    Methode um die EventListener auf dem Socket zu registrieren, aus dem übergebenen
    Mapping von EventNamen:String und Callbacks
     */
    private fun registerEventListeners(
        mapEventsToCallback: HashMap<String,
                (event: String, jsonObj: JSONObject) -> Unit>
    ) {
        for (k in mapEventsToCallback.keys) {
            mSocket?.on(k, Emitter.Listener {
                // parse the received data string into JSONObject (
                // TODO: Kommt vom Server ein String im JSONFORMat?
                var jsonData: JSONObject = JSONObject(it[0].toString())
                // Invoke the given callback with the parsed data
                mapEventsToCallback[k]?.invoke(k, jsonData)
            })
        }

        fun releaseConnection() {
            //TODO Was noch um alle Ressourcen freizugeben?
            mSocket?.close()
        }

    }

}
