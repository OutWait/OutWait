package edu.kit.outwait.server.socketHelper

import com.corundumstudio.socketio.AckRequest
import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIONamespace
import com.corundumstudio.socketio.listener.DataListener
import com.corundumstudio.socketio.listener.DisconnectListener
import edu.kit.outwait.server.protocol.Event

class SocketAdapter(val namespace: SocketIONamespace) {
    private val facades = hashMapOf<SocketIOClient, SocketFacade>()

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
                        println(
                            "SO-ADAPTER: New message received. Type " + e.getEventTag() +
                                ", data: " + dat
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
            println(
                "SO-ADAPTER: Registered listener for event " + e.getEventTag() + " in " +
                    javaClass.name
            )
        }

        // Configure disconnect listener
        namespace.addDisconnectListener(
            object : DisconnectListener {
                override fun onDisconnect(client: SocketIOClient) {
                    println("SO-ADAPTER: Client disconnected")
                    val facade = facades[client]
                    if (facade != null) {
                        removeFacade(facade)
                        facade.disconnectCallback()
                    }
                }
            }
        );
    }

    fun addFacadeForSocket(facade: SocketFacade, socket: SocketIOClient) {
        println("SO-ADAPTER: Adding new facade")
        facades.put(socket, facade)
    }
    private fun removeFacade(facade: SocketFacade) {
        println("SO-ADAPTER: Removing facade")
        facades.values.remove(facade)
    }
}
