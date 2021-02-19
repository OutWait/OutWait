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

       // registerEventListeners(mapEventToCallback)

        // TEST with second method
        registerEventListenersTEST(mapEventToCallback)

        Log.i("SocketAdapter", "All listeners were registered")

        /*
        Im Folgednen Implementierung von Listener für die ganzen Socket.IO seitigen Events
        wobei entweder Zustand gesetzt (CONNECT) oder Fehlermeldungen (DISCONNECT, ERROR)
        geworfen werden sollen
         */

        socketIOSocket.on(Socket.EVENT_CONNECT, Emitter.Listener {
            // TODO Was soll hier getan werden?
            Log.i("SocketAdapter", "Socket is connected")
        }
        )

        socketIOSocket.on(Socket.EVENT_CONNECT_ERROR, Emitter.Listener {
            // TODO Was soll hier getan werden?
            Log.i("SocketAdapter", "EVENT_CONNECT_ERROR")
        }
        )

        socketIOSocket.on(Socket.EVENT_ERROR, Emitter.Listener {
            // TODO Was soll hier getan werden?
            Log.i("SocketAdapter", "EVENT_ERROR")
        }
        )
        socketIOSocket.on(Socket.EVENT_CONNECT_TIMEOUT, Emitter.Listener {
            // TODO Was soll hier getan werden?
            Log.i("SocketAdapter", "EVENT_ERROR")
        }
        )


        //TODO Hierfür Exception werfen sinnvoll? -> erst wenn auch reconnect endgültig failed!!!!
        socketIOSocket.on(Socket.EVENT_DISCONNECT, Emitter.Listener {
            //TODO Was soll hier getan werden?
            Log.i("SocketAdapter", "Socket is disconnected")
        }
        )

        //TODO Was ist mit EVENT_DISCONNECT, EVENT_CONNECT_TIMEOUT, EVENT_ERROR, EVENT_RECONNECT etc?
        // soll Fehler geworfen werden, wenn Verbindung dauerhaft nicht möglich?

        // open the socket connection
        if(socketIOSocket.open() == null){
            Log.d("jkdfjagl", "isNull")
        }else{
            Log.d("jkdfjagl", "is not Null")
        }
        //socketIOSocket?.connect()

        Log.i("SocketAdapter", "SocketIOSocket.connect() was called")
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
    Methode um die EventListener auf dem Socket zu registrieren, aus dem übergebenen
    Mapping von Event: Event und Callbacks
     */
    private fun registerEventListeners(
        mapEventsToCallback: HashMap<Event,
                (wrappedJSONData: JSONObjectWrapper) -> Unit>
    ) {
        for (k in mapEventsToCallback.keys) {

            socketIOSocket.on(k.getEventString(), Emitter.Listener {

                // parse the received data string into JSONObject
                // TODO Test ob das wirklich so funktioniert

                Log.d("incoming event:", k.getEventString())
                val data = it[1] as String

                val jsonData = JSONObject(data)

                //val jsonData: JSONObject = JSONObject(it[0].toString())

                // wrap the parsed JSONObject with appropriate JSONObjectWrapper
                val wrappedJSONData = k.createWrapper(jsonData)

                // Invoke the given callback with the parsed data
                //TODO Funktioniert der Aufruf der Callback Methode richtig?
                mapEventsToCallback[k]?.invoke(wrappedJSONData)
            })
        }
    }


    /*
    Gibt die von SocketIOSocket gehaltene Verbindung frei
    //TODO Muss noch was gemacht/freigegeben werden?
     */
    fun releaseConnection() {
        socketIOSocket.close()
    }

    /*
    Gibt Auskunft ob die SOcketIOSocket Instanz momentan mit Server verbunden ist oder nicht
     */
    fun isConnected(): Boolean {
        return socketIOSocket.connected()
    }

    /*
    Testmethode um Listener zu registrieren
     */
    private fun registerEventListenersTEST(
        mapEventsToCallback: HashMap<Event,
                (wrappedJSONData: JSONObjectWrapper) -> Unit>
    ) {
        for (k in mapEventsToCallback.keys) {

            val onEventListenerCallback =

                Emitter.Listener { args ->
                        val data = args[1] as String
                        val jsonData = JSONObject(data)
                        val wrappedJSONData = k.createWrapper(jsonData)

                        Log.d("incoming event:", k.getEventString())

                        mapEventsToCallback[k]?.invoke(wrappedJSONData)
                    }

            socketIOSocket.on(k.getEventString(), onEventListenerCallback)
        }
    }


}
