package edu.kit.outwait.server.socketHelper

import com.corundumstudio.socketio.SocketIOClient
import edu.kit.outwait.server.protocol.Event
import edu.kit.outwait.server.protocol.JSONObjectWrapper

class SocketFacade(val socket: SocketIOClient, adapter: SocketAdapter) {
    internal val eventCallbacks = hashMapOf<Event, (receivedData: JSONObjectWrapper) -> Unit>()
    internal var disconnectCallback: () -> Unit = {}

    init {
        adapter.addFacadeForSocket(this, socket)
        println("SO-FACADE: Facade initialized")
    }

    fun send(event: Event, toSend: JSONObjectWrapper) {
        println(
            "SO-FaCADE: Sending event " + event.getEventTag() + " with data " +
                toSend.getJSONString()
        );
        socket.sendEvent(event.getEventTag(), toSend.getJSONString())
    }
    fun onReceive(event: Event, callback: (receivedData: JSONObjectWrapper) -> Unit) {
        eventCallbacks.put(event, callback)
    }
    fun onDisconnect(callback: () -> Unit) {
        disconnectCallback = callback
    }

    /** Closes the connection */
    fun disconnect() {
        println("SO-FACADE: Disconnecting socket")
        socket.disconnect()
    }
}
