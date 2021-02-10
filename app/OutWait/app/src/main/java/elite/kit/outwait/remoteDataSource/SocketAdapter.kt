package elite.kit.outwait.remoteDataSource

import android.util.Log
import elite.kit.outwait.networkProtocol.Event
import elite.kit.outwait.networkProtocol.JSONObjectWrapper
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URI


//TODO Fehler werfen bei Verbindungsfehler/Abbruch (inkl. der Listener)
//oder generell wann müssen/sollen Fehlermeldungen geworfen werden?
//TODO Was ist Socket.IO mäßig noch zu beachten?
//TODO Was noch um alle Ressourcen freizugeben?

class SocketAdapter(private val namespace: String) {

    private val serverURI: String = "http://127.0.0.1:8080"

    private val socketIOSocket: Socket? = null

    init {
        val options = IO.Options()
        options.reconnection = true

        val mSocket = IO.socket(URI.create(serverURI + namespace), options)
    }

    /*
    Intialisiere Verbindung mit Server
    Uund registriere die EventListener (in private Methode ausgelagert)
    //TODO Was ist Socket.IO mäßig noch zu beachten?
     */
    fun initializeConnection(mapEventsToCallback: HashMap<Event,
                (event: String, jsonObj: JSONObjectWrapper) -> Unit>) {

        registerEventListeners(mapEventsToCallback)

        //TODO Was ist mit on(CONNECT) und on(DISCONNECT) Event (-listeners) ?
        //TODO Testlog falls Socket erfolgreich connected
        socketIOSocket?.on(Socket.EVENT_CONNECT, Emitter.Listener {
            Log.i("SocketAdapter", "Socket is connected")
        }
        )

        //TODO Testlog falls Socket disconnected
        socketIOSocket?.on(Socket.EVENT_DISCONNECT, Emitter.Listener {
            Log.i("SocketAdapter", "Socket is disconnected")
        }
        )

        socketIOSocket?.connect()
    }


    /*
    Emitte Event mit Daten zum Server
     */
    fun emitEventToServer(event: String, wrappedJSONData: JSONObjectWrapper) {
        socketIOSocket?.emit(event, jsonData.toString())
    }

    /*
    Methode um die EventListener auf dem Socket zu registrieren, aus dem übergebenen
    Mapping von EventNamen:String und Callbacks
     */
    private fun registerEventListeners(
        mapEventsToCallback: HashMap<Event,
                (event: String, jsonObj: JSONObjectWrapper) -> Unit>
    ) {
        for (k in mapEventsToCallback.keys) {

            socketIOSocket?.on(k.getEventString(), Emitter.Listener {

                // parse the received data string into JSONObject (
                var jsonData: JSONObject = JSONObject(it[0].toString())

                // wrap the parsed JSONObject with appropriate JSONObjectWrapper
                val wrappedJSONData = k.createWrapper(jsonData)

                // Invoke the given callback with the parsed data
                //TODO Funktioniert der Aufruf der Callback Methode richtig?
                mapEventsToCallback[k]?.invoke(k.getEventString(), wrappedJSONData)
            })
        }

        fun releaseConnection() {
            //TODO Was noch um alle Ressourcen freizugeben?
            socketIOSocket?.close()
        }

    }

}
