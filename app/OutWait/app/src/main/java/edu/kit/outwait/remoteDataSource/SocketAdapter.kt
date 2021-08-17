package edu.kit.outwait.remoteDataSource

import android.util.Log
import edu.kit.outwait.networkProtocol.Event
import edu.kit.outwait.networkProtocol.JSONObjectWrapper
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import org.json.JSONObject
import java.net.URI

/**
 * Defines the maximum amount of time waited for an successfully established connection until
 * a time out
 */
private const val MAX_AMOUNT_CONNECT_WAITTIME = 5000L

/**
 * Defines the sampling granulation with which a successful established connection is checked
 */
private const val TIME_STEP_FOR_CONNECT_WAIT = 100L

/**
 * The server URI, used together with the namespace to connect to the "outwait server"
 */
private const val serverURI: String = "http://0.0.0.0:567" // TODO replace with server ip.

/**
 * This class serves as a facade to the underlying websocket implementation of the
 * SocketIO library, used by the SocketIOClient- and ManagementHandler.
 * It emits and receives events and invokes their respective callbacks,
 * after a successful connection to the server was established.
 *
 * @constructor
 * The socket.io socket instance is constructed as a client or management instance depending
 * on the given namespace
 *
 * @param namespace as String, specifies if socket connection is client or management connection
 */
class SocketAdapter(namespace: String) {

    /**
     * The actual socket instance of the SocketIO library
     */
    private val socketIOSocket: Socket

    /**
     * A state variable to detect if a network error occurred and the connection
     * was closed and should therefore be released
     */
    private var errorReceived = false


    init {
        //Configure options of the socket.io socket
        val options = IO.Options()
        options.transports = arrayOf(WebSocket.NAME)
        options.reconnection = true

        socketIOSocket = IO.socket(URI.create(serverURI + namespace), options)
        Log.i("SocketAdapter", "SocketIOSocket was created")
    }

    /**
     * This method registers the given events and callbacks as listeners on the socket
     * and then opens the connection to the server
     *
     * @param mapEventToCallback HashMap that maps events of type Event to their respective
     * callback (which takes a wrapped JSON object as input)
     * @return true if socket connection was successfully established, else false
     */
    fun initializeConnection(mapEventToCallback: HashMap<Event,
                (wrappedJSONData: JSONObjectWrapper) -> Unit>) : Boolean {

        // register remaining SocketIO related connection listeners for log purposes
        registerRemainingSocketIOListeners()

        // register given listeners and their events
        registerEventListeners(mapEventToCallback)

        // open the socket connection
        if (this.socketIOSocket.connect() == null) {
            Log.d("SocketAdapter", "Socket connection could not be established")
            releaseConnection()
            return false
        }

        // wait for established connection until time out
        var waitedTimeToConnect = 0L
        while (!this.socketIOSocket.connected() and (waitedTimeToConnect < MAX_AMOUNT_CONNECT_WAITTIME)) {
            waitedTimeToConnect += TIME_STEP_FOR_CONNECT_WAIT
            Thread.sleep(TIME_STEP_FOR_CONNECT_WAIT)
        }
        if (this.socketIOSocket.connected()) {
            Log.d("SocketAdapter", "Socket successfully connected")
            return true
        } else {
            Log.d("SocketAdapter", "Socket couldn't connect until time out")
            releaseConnection()
        }
        return false
    }

    /**
     * This method emits events and their respective data to the server using the
     * emit/send function of the socket.io socket
     *
     * @param event as String, specifies the event that is being transmitted
     * @param wrappedJSONData associated data, transmitted as JSONString
     */
    fun emitEventToServer(event: String, wrappedJSONData: JSONObjectWrapper) {
        this.socketIOSocket.emit(event, wrappedJSONData.getJSONString())
        Log.i("SocketAdapter", "Event $event was emitted to server")
    }

    /**
     * This method closes the current connection and removes all previously registered listeners
     */
    fun releaseConnection() {
        this.socketIOSocket.close()
        // remove all registered listeners
        this.socketIOSocket.off()
        // reset error state
        this.errorReceived = false
        Log.i("SocketAdapter", "Socket connection was closed")
    }

    /**
     * This method takes the given mapping of Events and their respective callbacks and registers
     * the listeners on the socket.io socket accordingly
     *
     * @param mapEventsToCallback HashMap that maps events of type Event to their respective
     * callback (which takes a wrapped JSON object as input)
     */
    private fun registerEventListeners(mapEventsToCallback: HashMap<Event,
                (wrappedJSONData: JSONObjectWrapper) -> Unit>) {

        // register socket.io own listeners for network error callback
        this.socketIOSocket.on(Socket.EVENT_ERROR, Emitter.Listener {
            Log.i("SocketAdapter", "Event " + Socket.EVENT_ERROR)
            this.errorReceived = true
        })

        this.socketIOSocket.on(Socket.EVENT_DISCONNECT, Emitter.Listener {
            Log.i("SocketAdapter", "Event " + Socket.EVENT_DISCONNECT)
            if(errorReceived) {
                errorReceived = false
                // error occurred, connection session is irrevocably lost on disconnect
                val wrappedEmpty = Event.NETWORK_ERROR.createWrapper(JSONObject())
                mapEventsToCallback[Event.NETWORK_ERROR]?.invoke(wrappedEmpty)
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

                    Log.d("SocketAdapter","Incoming event:" + k.getEventString())

                    // Invoke the given callback with the parsed data
                    mapEventsToCallback[k]?.invoke(wrappedJSONData)
                }

            // register the listener on the socket
            this.socketIOSocket.on(k.getEventString(), onEventListenerCallback)
        }
    }

    // register remaining SocketIO related connection listeners for log purposes
    private fun registerRemainingSocketIOListeners() {

        // called on successful connection or reconnection
        val onConnectCallback = Emitter.Listener {
            Log.i("SocketAdapter", "Event " + Socket.EVENT_CONNECT)
        }
        this.socketIOSocket.on(Socket.EVENT_CONNECT, onConnectCallback)

        // called if socket.io automatically tries to reconnect (but unsuccessful)
        val onConnectErrorCallback = Emitter.Listener {
            Log.i("SocketAdapter", "Event " + Socket.EVENT_CONNECT_ERROR)
        }
        this.socketIOSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectErrorCallback)

        val onEventConnectTimeoutCallback = Emitter.Listener {
            Log.i("SocketAdapter", "Event " + Socket.EVENT_CONNECT_TIMEOUT)
        }
        this.socketIOSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onEventConnectTimeoutCallback)
    }
}
