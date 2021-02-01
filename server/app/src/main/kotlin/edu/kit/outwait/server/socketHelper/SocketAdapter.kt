package edu.kit.outwait.socketHelper

class SocketAdapter(namespace: SocketIONamespace) {
    private facades: HashMap<SocketIOClient, SocketFacade>

    fun configureEvents(events: List<Event>) {}
    fun addFacadeForSocket(facade: SocketFacade, socket: SocketIOClient) {}
    private fun removeFacade(facade: SocketFacade) {}
}
