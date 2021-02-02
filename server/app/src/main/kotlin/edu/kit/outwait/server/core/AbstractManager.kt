package edu.kit.outwait.server.core

import com.corundumstudio.socketio.SocketIONamespace
import com.corundumstudio.socketio.SocketIOClient

import edu.kit.outwait.server.socketHelper.SocketAdapter
import edu.kit.outwait.server.socketHelper.SocketFacade
import edu.kit.outwait.server.protocol.Event

abstract class AbstractManager(namespace: SocketIONamespace, protected val databaseWrapper: DatabaseWrapper) {
    protected val socketAdapter = SocketAdapter(namespace)

    abstract fun bindSocket(socket: SocketIOClient, socketFacade: SocketFacade)

    private fun configureConnectionCreation() {

    }

    fun configureEventListeners(events: List<Event>) {

    }

}
