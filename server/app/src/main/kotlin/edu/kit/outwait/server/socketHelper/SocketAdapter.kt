package edu.kit.outwait.server.socketHelper

import com.corundumstudio.socketio.AckRequest
import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIONamespace
import com.corundumstudio.socketio.listener.DataListener
import com.corundumstudio.socketio.listener.DisconnectListener
import edu.kit.outwait.server.core.Logger
import edu.kit.outwait.server.protocol.Event

class SocketAdapter(val namespace: SocketIONamespace) {
    private val facades = hashMapOf<SocketIOClient, SocketFacade>()
    private val LOG_ID = "SO-ADAPTER"

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

    fun addFacadeForSocket(facade: SocketFacade, socket: SocketIOClient) {
        Logger.debug(LOG_ID, "Adding new facade")
        facades.put(socket, facade)
    }
    private fun removeFacade(facade: SocketFacade) {
        Logger.debug(LOG_ID, "Removing facade")
        facades.values.remove(facade)
    }
}
