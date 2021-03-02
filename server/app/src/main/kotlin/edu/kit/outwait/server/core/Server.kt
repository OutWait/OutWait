package edu.kit.outwait.server.core

import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.SocketIOServer
import edu.kit.outwait.server.client.ClientManager
import edu.kit.outwait.server.management.ManagementManager

class Server {
    private val server: SocketIOServer
    private val clientManager: ClientManager
    private val managementManager: ManagementManager
    private val LOG_ID = "SERVER"

    init {
        // First The database
        Logger.debug(LOG_ID, "Creating database")
        val databaseWrapper = DatabaseWrapper("OutwaitDB")

        // Now the io server
        Logger.debug(LOG_ID, "Creating server (socketIO)")
        val config = Configuration()
        config.setHostname("0.0.0.0")
        config.setPort(567)
        config.setBossThreads(1)
        config.setWorkerThreads(1)
        config.pingInterval = 5000
        config.pingTimeout = 120000

        server = SocketIOServer(config)

        Logger.debug(LOG_ID, "Creating namespaces")
        val clientNamespace = server.addNamespace("/client")
        val managementNamespace = server.addNamespace("/management")

        // Finally our own classes
        Logger.debug(LOG_ID, "Creating managers")
        clientManager = ClientManager(clientNamespace, databaseWrapper)
        managementManager = ManagementManager(managementNamespace, databaseWrapper)

        Logger.info(LOG_ID, "Server initialized")
    }

    fun run() {
        Logger.info(LOG_ID, "Starting server...")
        server.start()
        Logger.info(LOG_ID, "Server started.")
    }

    fun stop() {
        Logger.info(LOG_ID, "Stopping server...")
        server.stop()
        Logger.info(LOG_ID, "Starting stopped.")
    }
}

fun main() {
    Logger.debug("MAIN", "Entry point reached")
    val server = Server()
    server.run()
    Logger.debug("MAIN", "Main terminated")
}
