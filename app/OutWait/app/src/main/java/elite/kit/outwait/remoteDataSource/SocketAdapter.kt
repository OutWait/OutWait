package elite.kit.outwait.remoteDataSource

import android.util.Log
import elite.kit.outwait.networkProtocol.Event
import elite.kit.outwait.networkProtocol.JSONObjectWrapper
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URI


//TODO Fehler werfen bei Verbindungsfehler/Abbruch (inkl. der Listener) usw. ?
//TODO Was ist Socket.IO mäßig noch zu beachten?
//TODO Welche Zustände sollen/müssen alles hier gehalten werden?

class SocketAdapter(private val namespace: String) {

    private val serverURI: String = "http://161.97.168.24:567"

    private val socketIOSocket: Socket

    init {
        val options = IO.Options()
        options.reconnection = true

        socketIOSocket = IO.socket(URI.create(serverURI + namespace), options)
        Log.i("SocketAdapter", "SocketIOSocket was created")
    }

    /*
    Intialisiere Verbindung mit Server
    Uund registriere die EventListener (in private Methode ausgelagert)
     */
    fun initializeConnection(
        mapEventToCallback: HashMap<Event,
                (wrappedJSONData: JSONObjectWrapper) -> Unit>
    ) {
        // register SocketIO related connection listeners
        registerSocketIOListeners()

        // register given listeners and their events
        registerEventListeners(mapEventToCallback)

        Log.i("SocketAdapter", "All listeners were registered")

        // open the socket connection
        if (socketIOSocket.open() == null) {
            Log.d("jkdfjagl", "isNull")
        } else {
            Log.d("jkdfjagl", "is not Null")
        }

        Log.i("SocketAdapter", "SocketIOSocket.open() was called")
    }

    // TODO Fürs parsen hier eine JSON Exception werfen, falls es nicht klappt
    /*
    Emitte Event mit Daten zum Server
     */
    fun emitEventToServer(event: String, wrappedJSONData: JSONObjectWrapper) {
        socketIOSocket.emit(event, wrappedJSONData.getJSONString())
        Log.i("SocketAdapter", "Event $event was emitted to server on SocketIOSocket")
    }

    /*
    Gibt die von SocketIOSocket gehaltene Verbindung frei und entfernt alle Listener
     */
    fun releaseConnection() {
        socketIOSocket.close()

        // remove all registered listeners
        socketIOSocket.off()
    }

    /*
    Gibt Auskunft ob die SOcketIOSocket Instanz momentan mit Server verbunden ist oder nicht
     */
    fun isConnected(): Boolean {
        return socketIOSocket.connected()
    }

    /*
    Methode um die EventListener auf dem Socket zu registrieren, aus dem übergebenen
    Mapping von Event: Event und Callbacks
     */
    private fun registerEventListeners(
        mapEventsToCallback: HashMap<Event,
                (wrappedJSONData: JSONObjectWrapper) -> Unit>
    ) {
        for (k in mapEventsToCallback.keys) {

            val onEventListenerCallback =

                Emitter.Listener { args ->

                    // parse the received data string as JSONObject
                    val data = args.last() as String
                    val jsonData = JSONObject(data)

                    // wrap the parsed JSONObject with appropriate JSONObjectWrapper
                    val wrappedJSONData = k.createWrapper(jsonData)

                    Log.d("incoming event:", k.getEventString())

                    // Invoke the given callback with the parsed data
                    //TODO Funktioniert der Aufruf der Callback Methode richtig?
                    mapEventsToCallback[k]?.invoke(wrappedJSONData)
                }

            // register the listener
            socketIOSocket.on(k.getEventString(), onEventListenerCallback)
        }
    }

    /*
        Im Folgednen Implementierung von Listener für die ganzen Socket.IO seitigen Events
        wobei entweder Zustand gesetzt (CONNECT) oder Fehlermeldungen (DISCONNECT, ERROR)
        geworfen werden sollen and keep track of all registred listeners for later removal
         */
    private fun registerSocketIOListeners() {

        val onConnectCallback = Emitter.Listener {
            Log.i("SocketAdapter", "Event" + Socket.EVENT_CONNECT)
        }
        socketIOSocket.on(Socket.EVENT_CONNECT, onConnectCallback)

        val onConnectErrorCallback = Emitter.Listener {
            Log.i("SocketAdapter", "Event" + Socket.EVENT_CONNECT_ERROR)
        }
        socketIOSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectErrorCallback)

        val onEventErrorCallback = Emitter.Listener {
            Log.i("SocketAdapter", "Event" + Socket.EVENT_ERROR)
        }
        socketIOSocket.on(Socket.EVENT_ERROR, onEventErrorCallback)

        val onEventConnectTimeoutCallback = Emitter.Listener {
            Log.i("SocketAdapter", "Event" + Socket.EVENT_CONNECT_TIMEOUT)
        }
        socketIOSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onEventConnectTimeoutCallback)

        //TODO Hierfür Exception werfen sinnvoll? -> erst wenn auch reconnect endgültig failed!!!!
        val onEventDisconnectCallback = Emitter.Listener {
            Log.i("SocketAdapter", "Event" + Socket.EVENT_DISCONNECT)
        }
        socketIOSocket.on(Socket.EVENT_DISCONNECT, onEventDisconnectCallback)

        //TODO Was ist mit EVENT_DISCONNECT, EVENT_CONNECT_TIMEOUT, EVENT_ERROR, EVENT_RECONNECT etc?
        // soll Fehler geworfen werden, wenn Verbindung dauerhaft nicht möglich?

    }

}
