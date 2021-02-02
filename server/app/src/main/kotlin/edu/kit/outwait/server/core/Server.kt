package edu.kit.outwait.server.core

import com.corundumstudio.socketio.AckRequest
import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIONamespace
import com.corundumstudio.socketio.SocketIOServer
import com.corundumstudio.socketio.listener.DataListener

import edu.kit.outwait.server.client.ClientManager
import edu.kit.outwait.server.management.ManagementManager

class Server {
    val server: SocketIOServer
    val clientManager: ClientManager
    val managementManager: ManagementManager

    init {
        // First The database
        val databaseWrapper = DatabaseWrapper()

        // Now the io server
		val config = Configuration();
		config.setHostname("127.0.0.1");
		config.setPort(8080);

		server = SocketIOServer(config);

        val clientNamespace = server.addNamespace("/client");
        val managementNamespace = server.addNamespace("/management");

        // Finally our own classes
        clientManager = ClientManager(clientNamespace, databaseWrapper)
        managementManager = ManagementManager(managementNamespace, databaseWrapper)
    }

    fun run() {
        println("Hello World from server!")
        server.start();
    }
}

fun main() {
    val server = Server()
    server.run()
}
