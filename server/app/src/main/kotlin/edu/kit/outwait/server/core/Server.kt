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
        val databaseWrapper = DatabaseWrapper()

        // Now the io server
        val config = Configuration();
        config.setHostname("127.0.0.1");
        config.setPort(8080);
        config.setBossThreads(1);
        config.setWorkerThreads(1);

        server = SocketIOServer(config);

        val clientNamespace = server.addNamespace("/client");
        val managementNamespace = server.addNamespace("/management");

        // Finally our own classes
        clientManager = ClientManager(clientNamespace, databaseWrapper)
        managementManager = ManagementManager(managementNamespace, databaseWrapper)
    }

    fun run() {
        println("Hello World from server!")

        // DEBUG terminate the server after 10 seconds
        /*java.util
            .Timer()
            .schedule(
                object : java.util.TimerTask() {
                    override fun run() {
                        stop()
                        System.exit(0)
                    }
                },
                10000
            )*/

        server.start()
    }

    fun stop() {
        server.stop()
    }
}

fun main() {
    val server = Server()
    server.run()
}
