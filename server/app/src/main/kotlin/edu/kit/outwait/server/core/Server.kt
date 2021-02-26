package edu.kit.outwait.server.core

import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.SocketIOServer
import edu.kit.outwait.server.client.ClientManager
import edu.kit.outwait.server.management.ManagementManager

class Server {
    val server: SocketIOServer
    val clientManager: ClientManager
    val managementManager: ManagementManager

    init {
        // First The database
        println("Creating database")
        val databaseWrapper = DatabaseWrapper()

        // Now the io server
        println("Creating server (socketIO)")
        val config = Configuration()
        config.setHostname("0.0.0.0")
        config.setPort(567)
        config.setBossThreads(1)
        config.setWorkerThreads(1)
        config.pingInterval = 5000
        config.pingTimeout = 120000

        server = SocketIOServer(config)

        println("Creating namespaces")
        val clientNamespace = server.addNamespace("/client")
        val managementNamespace = server.addNamespace("/management")

        // Finally our own classes
        println("Creating managers")
        clientManager = ClientManager(clientNamespace, databaseWrapper)
        managementManager = ManagementManager(managementNamespace, databaseWrapper)

        println("Server initialized")
    }

    fun run() {
        println("Starting server...")
        server.start()
        println("Server started.")
    }

    fun stop() {
        println("Stopping server...")
        server.stop()
        println("Starting stopped.")
    }
}

fun main() {
    println("Entry point reached")
    val server = Server()
    server.run()
    println("Main terminated")
}
