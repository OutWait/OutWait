package edu.kit.outwait.server.core

import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIONamespace
import com.corundumstudio.socketio.listener.ConnectListener;
import edu.kit.outwait.server.protocol.Event
import edu.kit.outwait.server.socketHelper.SocketAdapter
import edu.kit.outwait.server.socketHelper.SocketFacade

abstract class AbstractManager(
    namespace: SocketIONamespace,
    protected val databaseWrapper: DatabaseWrapper
) {
    protected val socketAdapter = SocketAdapter(namespace)

    init {
        configureConnectionCreation(namespace)
    }

    abstract fun bindSocket(socket: SocketIOClient, socketFacade: SocketFacade)

    private fun configureConnectionCreation(namespace: SocketIONamespace) {
        namespace.addConnectListener(
            object : ConnectListener {
                override fun onConnect(client: SocketIOClient) {
                    val socketFacade = SocketFacade(client, socketAdapter);
                    bindSocket(client, socketFacade);
                }
            }
        );
    }

    fun configureEventListeners(events: List<Event>) {
        socketAdapter.configureEvents(events)
    }
}
