package edu.kit.outwait.server.socketHelper

import com.corundumstudio.socketio.SocketIONamespace
import com.corundumstudio.socketio.SocketIOClient
import edu.kit.outwait.server.protocol.Event

class SocketAdapter(namespace: SocketIONamespace) {
    private val facades = hashMapOf<SocketIOClient, SocketFacade>()

    fun configureEvents(events: List<Event>) {}
    fun addFacadeForSocket(facade: SocketFacade, socket: SocketIOClient) {}
    private fun removeFacade(facade: SocketFacade) {}
}
