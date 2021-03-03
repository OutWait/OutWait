package edu.kit.outwait.server.core

import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIONamespace
import com.corundumstudio.socketio.listener.ConnectListener
import edu.kit.outwait.server.protocol.Event
import edu.kit.outwait.server.socketHelper.SocketAdapter
import edu.kit.outwait.server.socketHelper.SocketFacade

/**
 * Base class for ClientManager and ManagementManager.
 *
 * Implements common operations, like connection creation and holds common data.
 *
 * @param namespace the SocketIONamespace to which new clients of this manager connect.
 * @property databaseWrapper the DB to load and store data.
 * @constructor Configures the creation of connections.
 */
abstract class AbstractManager(
    namespace: SocketIONamespace,
    protected val databaseWrapper: DatabaseWrapper
) {
    protected val socketAdapter = SocketAdapter(namespace)
    private val LOG_ID = "ABS-MGR"

    init {
        configureConnectionCreation(namespace)
    }

    /**
     * Abstract method which is called when a new socket connection was established.
     *
     * @param socketFacade the socketFacade to send and receive messages over the new connection.
     */
    abstract fun bindSocket(socketFacade: SocketFacade)

    /**
     * Internal method to configure the connection routine.
     *
     * @param namespace the SocketIONamespace to bind to.
     */
    private fun configureConnectionCreation(namespace: SocketIONamespace) {
        namespace.addConnectListener(
            object : ConnectListener {
                override fun onConnect(client: SocketIOClient) {
                    Logger.debug(
                        LOG_ID,
                        "New socket connection established with id " + client.getSessionId()
                    )
                    val socketFacade = SocketFacade(client, socketAdapter)
                    bindSocket(socketFacade)
                }
            }
        )
        Logger.debug(LOG_ID, "Configured welcome socket in " + this.javaClass.typeName)
    }

    /**
     * Configures the incoming event types for this manager.
     *
     * Call this method before starting the server.
     *
     * @param events the list of supported events by this manager.
     */
    fun configureEventListeners(events: List<Event>) {
        socketAdapter.configureEvents(events)
    }
}
