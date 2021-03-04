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

private const val MAX_AMOUNT_CONNECT_WAITTIME = 10000L
private const val TIME_STEP_FOR_CONNECT_WAIT = 1000L

class SocketAdapter(namespace: String) {

    private val serverURI: String = "http://161.97.168.24:567"

    private val socketIOSocket: Socket

    private var errorReceived = false

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
    ) : Boolean {
        // register remaining SocketIO related connection listeners
        // registerRemainingSocketIOListeners() //TODO brauchen wir nicht mehr

        // register given listeners and their events
        registerEventListeners(mapEventToCallback)

        Log.i("SocketAdapter", "All listeners were registered")

        // open the socket connection (try until max time waited)

        if (socketIOSocket.connect() == null) {
            Log.d("SocketAdapter", "socket could not connect for first time")
            releaseConnection()
            return false
        }
        var waitedTimeToConnect = 0L
        while (!isConnected() and (waitedTimeToConnect < MAX_AMOUNT_CONNECT_WAITTIME)) {
            waitedTimeToConnect += TIME_STEP_FOR_CONNECT_WAIT
            Log.i("SocketAdaper", "Waiting for connection establishing since $waitedTimeToConnect millis")
            Thread.sleep(TIME_STEP_FOR_CONNECT_WAIT)
        }
        if (isConnected()) {
            Log.d("SocketAdapter", "socket successfully connected for first time")
            return true
        }
        Log.d("SocketAdapter", "Socket couldnt connect for first time (tried for  $waitedTimeToConnect millis)")
        releaseConnection()
        return false
    }


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
        Log.i("SocketAdapter", "Socket was closed, all listeners removed")
    }

    /*
    Gibt Auskunft ob die SOcketIOSocket Instanz momentan mit Server verbunden ist oder nicht
     */
    fun isConnected(): Boolean {
        return socketIOSocket.connected()
    }

    /* TODO Brauchen wir das schon/noch
    fun getCurrentSessionID(): String {
        return socketIOSocket.id()
    }

     */

    /*
    Methode um die EventListener auf dem Socket zu registrieren, aus dem übergebenen
    Mapping von Event: Event und Callbacks
     */
    private fun registerEventListeners(
        mapEventsToCallback: HashMap<Event,
                (wrappedJSONData: JSONObjectWrapper) -> Unit>
    ) {
        socketIOSocket.on(Socket.EVENT_ERROR, Emitter.Listener {
            Log.i("SocketAdapter", "Event " + Socket.EVENT_ERROR)
            this.errorReceived = true
        })

        socketIOSocket.on(Socket.EVENT_DISCONNECT, Emitter.Listener {
            Log.i("SocketAdapter", "Event " + Socket.EVENT_DISCONNECT)
            if(errorReceived) {
                val wrappedEmpty = Event.NETWORK_ERROR.createWrapper(JSONObject())
                mapEventsToCallback[Event.NETWORK_ERROR]?.invoke(wrappedEmpty)
                errorReceived = false
            }
        })

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
                    mapEventsToCallback[k]?.invoke(wrappedJSONData)
                }

            // register the listener
            socketIOSocket.on(k.getEventString(), onEventListenerCallback)
        }
    }

    /*
        Im Folgednen Implementierung von Listener für die ganzen Socket.IO seitigen Events
        wobei entweder Zustand gesetzt (CONNECT) oder Fehlermeldungen (DISCONNECT, ERROR)
        geworfen werden sollen
        //TODO Brauchen wir das noch? -> bisher nur zu Log-Zwecken
         */
    private fun registerRemainingSocketIOListeners() {

        // called on successful connection or reconnection
        val onConnectCallback = Emitter.Listener {
            Log.i("SocketAdapter", "Event " + Socket.EVENT_CONNECT)
        }
        socketIOSocket.on(Socket.EVENT_CONNECT, onConnectCallback)

        // called wenn SocketIO erfolglos (selber) versucht zu (re)connecten
        val onConnectErrorCallback = Emitter.Listener {
            Log.i("SocketAdapter", "Event " + Socket.EVENT_CONNECT_ERROR)
        }
        socketIOSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectErrorCallback)

        val onEventConnectTimeoutCallback = Emitter.Listener {
            Log.i("SocketAdapter", "Event " + Socket.EVENT_CONNECT_TIMEOUT)
        }
        socketIOSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onEventConnectTimeoutCallback)
    }
}
