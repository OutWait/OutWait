package edu.kit.outwait.server.core

import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.SocketIOServer
import edu.kit.outwait.server.client.ClientManager
import edu.kit.outwait.server.management.ManagementManager

/**
 * The main server application, that represents the entry point.
 *
 * It holds the SocketIOServer and the managers.
 *
 * @constructor Initializes the whole server and loads the Database.
 */
class Server {
    private val server: SocketIOServer
    private val clientManager: ClientManager
    private val managementManager: ManagementManager
    private val LOG_ID = "SERVER"

    init {
        // First The database
        Logger.debug(LOG_ID, "Creating database")
        val databaseWrapper = DatabaseWrapper("OutwaitDB", "localhost")

        // Now the io server
        Logger.debug(LOG_ID, "Creating server (socketIO)")
        val config = Configuration()
        config.setHostname("0.0.0.0")
        config.setPort(567)
        config.setBossThreads(1)
        config.setWorkerThreads(1)
        config.pingInterval = 10000
        config.pingTimeout = 25000
        config.getSocketConfig().setReuseAddress(true)

        server = SocketIOServer(config)

        Logger.debug(LOG_ID, "Creating namespaces")
        val clientNamespace = server.addNamespace("/client")
        val managementNamespace = server.addNamespace("/management")

        // Finally our own classes
        Logger.debug(LOG_ID, "Creating managers")
        clientManager = ClientManager(clientNamespace, databaseWrapper)
        managementManager = ManagementManager(managementNamespace, databaseWrapper)

        // Add termination routine to close all connections gracefully
        Runtime.getRuntime().addShutdownHook(Thread { stop_server() })

        Logger.info(LOG_ID, "Server initialized")
    }

    /** Starts the server, so that it can receive requests. */
    fun start_server() {
        Logger.info(LOG_ID, "Starting server...")
        server.start()
        Logger.info(LOG_ID, "Server started.")
    }

    /** Shuts down the server explicitly. */
    fun stop_server() {
        Logger.info(LOG_ID, "Stopping server...")
        server.stop()
        server.getAllClients().forEach { it.disconnect() }
        Logger.info(LOG_ID, "Server stopped.")
    }
}

/**
 * Main entry point of the application.
 *
 * Creates and starts the server instance.
 */
fun main() {
    Logger.debug("MAIN", "Entry point reached")
    val server = Server()
    server.start_server()
    Logger.debug("MAIN", "Main terminated")
}
