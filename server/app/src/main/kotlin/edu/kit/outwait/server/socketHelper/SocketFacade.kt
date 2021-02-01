package edu.kit.outwait.socketHelper

class SocketFacade(socket: SocketIOClient, adapter: SocketAdapter) {
    private socket: SocketIOClient
    internal eventCallbacks: HashMap<Event, (receivedData: JSONObjectWrapper) -> Unit>
    internal disconnectCallback: () -> Unit

    fun send(event: Event, toSend: JSONObjectWrapper) {}
    fun onReceive(event: Event, callback: (receivedData: JSONObjectWrapper) -> Unit) {}
    fun onDisconnect(event: Event, callback: () -> Unit) {}

}
