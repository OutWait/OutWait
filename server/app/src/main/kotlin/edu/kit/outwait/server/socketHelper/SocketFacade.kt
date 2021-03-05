package edu.kit.outwait.server.socketHelper

import com.corundumstudio.socketio.SocketIOClient
import edu.kit.outwait.server.core.Logger
import edu.kit.outwait.server.protocol.Event
import edu.kit.outwait.server.protocol.JSONObjectWrapper

/**
 * Provides a uniform interface to a socket.
 *
 * This allows to easily send messages and register receivers event after the server has been
 * started.
 *
 * @property socket the SocketIOClient to identify the socket.
 * @param adapter the SocketAdapter in which the facade should register itself.
 * @constructor Registers the facade in the adapter.
 */
class SocketFacade(val socket: SocketIOClient, adapter: SocketAdapter) {
    internal val eventCallbacks = hashMapOf<Event, (receivedData: JSONObjectWrapper) -> Unit>()
    internal var disconnectCallback: () -> Unit = {}
    private val LOG_ID = "SO-FACADE"

    init {
        adapter.addFacadeForSocket(this, socket)
        Logger.debug(LOG_ID, "Facade initialized")
    }

    /**
     * Sends a message to the remote.
     *
     * @param event the type of event to send. The remote can use this to identify the message type.
     * @param toSend the data of the message packed in a json wrapper.
     */
    fun send(event: Event, toSend: JSONObjectWrapper) {
        Logger.debug(
            LOG_ID,
            "Sending event " + event.getEventTag() + " with data " + toSend.getJSONString()
        )
        socket.sendEvent(event.getEventTag(), toSend.getJSONString())
    }

    /**
     * Registers a handler for an event.
     *
     * @param event the type of event that should be handled by the function.
     * @param callback the function that should be executed when the event is received. The data is
     *     passed as parameter to this function.
     */
    fun onReceive(event: Event, callback: (receivedData: JSONObjectWrapper) -> Unit) {
        eventCallbacks.put(event, callback)
    }

    /**
     * Registers a handler for a disconnect event.
     *
     * @param callback the function that should be executed when the connection is closed.
     */
    fun onDisconnect(callback: () -> Unit) {
        disconnectCallback = callback
    }

    /** Closes the connection */
    fun disconnect() {
        Logger.debug(LOG_ID, "Disconnecting socket")
        if (socket.isChannelOpen()) socket.disconnect()
    }
}
