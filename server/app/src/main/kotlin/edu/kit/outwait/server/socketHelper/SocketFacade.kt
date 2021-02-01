package edu.kit.outwait.server.socketHelper

import com.corundumstudio.socketio.SocketIOClient
import edu.kit.outwait.server.protocol.JSONObjectWrapper
import edu.kit.outwait.server.protocol.Event

class SocketFacade(val socket: SocketIOClient, adapter: SocketAdapter) {
    internal val eventCallbacks = hashMapOf<Event, (receivedData: JSONObjectWrapper) -> Unit>()
    internal val disconnectCallback: () -> Unit = {}

    fun send(event: Event, toSend: JSONObjectWrapper) {}
    fun onReceive(event: Event, callback: (receivedData: JSONObjectWrapper) -> Unit) {}
    fun onDisconnect(event: Event, callback: () -> Unit) {}

}
