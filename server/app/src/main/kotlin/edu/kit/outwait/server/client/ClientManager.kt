package edu.kit.outwait.server.client

import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIONamespace
import edu.kit.outwait.server.core.AbstractManager
import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.slot.SlotCode
import edu.kit.outwait.server.socketHelper.SocketFacade

class ClientManager(namespace: SocketIONamespace, databaseWrapper: DatabaseWrapper) :
    AbstractManager(namespace, databaseWrapper) {

    private val clients = listOf<Client>()

    override fun bindSocket(socket: SocketIOClient, socketFacade: SocketFacade) {
    }

    fun removeClient(client: Client) {
    }

    fun registerReceiver(slotCode: SlotCode, receiver: SlotInformationReceiver) {
    }

    fun removeReceiver(receiver: SlotInformationReceiver) {
    }
}
