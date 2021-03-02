package edu.kit.outwait.server.socketHelper

import com.corundumstudio.socketio.AckRequest
import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIONamespace
import com.corundumstudio.socketio.listener.DataListener
import com.corundumstudio.socketio.listener.DisconnectListener
import edu.kit.outwait.server.core.Logger
import edu.kit.outwait.server.protocol.Event

/**
 * Coordinator for incoming messages over a socket.
 *
 * This class is used as a wrapper around the namespace-logic of SocketIO. SocketFacades register in
 * the adapter and incoming events are forwarded to the right handler of the right SocketFacade.
 * Each SocketAdapter wraps exactly one namespace.
 *
 * @param namespace the SocketIONamespace to wrap.
 * @constructor Creates a socket adapter object.
 */
class SocketAdapter(val namespace: SocketIONamespace) {
    private val facades = hashMapOf<SocketIOClient, SocketFacade>()
    private val LOG_ID = "SO-ADAPTER"

    /**
     * Configures all allowed incoming events.
     *
     * Call this method before starting the server.
     *
     * @param events specifies which events are allowed and handled by this adapter.
     */
    fun configureEvents(events: List<Event>) {
        // Configure all event listeners
        for (e in events) {
            namespace.addEventListener(
                e.getEventTag(),
                String::class.java,
                object : DataListener<String> {
                    override fun onData(
                        client: SocketIOClient,
                        dat:String,
                        ackRequest: AckRequest
                    ) {
                        Logger.debug(
                            LOG_ID,
                            "New message received from socket id " + client.getSessionId() +
                                ". Type " + e.getEventTag() + ", data: " + dat
                        )
                        val jsonWrapper = e.createWrapper(dat)
                        val facade = facades[client]
                        if (facade != null) {
                            val callback = facade.eventCallbacks[e]
                            if (callback != null) {
                                callback(jsonWrapper)
                            }
                        }
                    }
                }
            )
            Logger.debug(
                LOG_ID,
                "Registered listener for event " + e.getEventTag() + " in " + javaClass.name
            )
        }

        // Configure disconnect listener
        namespace.addDisconnectListener(
            object : DisconnectListener {
                override fun onDisconnect(client: SocketIOClient) {
                    Logger.debug(LOG_ID, "Client disconnected with id " + client.getSessionId())
                    val facade = facades[client]
                    if (facade != null) {
                        removeFacade(facade)
                        facade.disconnectCallback()
                    }
                }
            }
        )
    }

    /**
     * Register a new SocketFacade for this adapter.
     *
     * This can be called after the server has been started, to register new sockets. After that
     * incoming events are directed to the facade respectively.
     *
     * @param facade the SocketFacade to register
     * @param socket the SocketIOClient to identify the socket
     */
    fun addFacadeForSocket(facade: SocketFacade, socket: SocketIOClient) {
        Logger.debug(LOG_ID, "Adding new facade")
        facades.put(socket, facade)
    }

    /**
     * Called internally when a connection is closed to deregister a SocketFacade
     *
     * @facade the SocketFacade to deregister.
     */
    private fun removeFacade(facade: SocketFacade) {
        Logger.debug(LOG_ID, "Removing facade")
        facades.values.remove(facade)
    }
}
