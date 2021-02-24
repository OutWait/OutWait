package edu.kit.outwait.server.client

import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIONamespace
import edu.kit.outwait.server.core.AbstractManager
import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.protocol.Event
import edu.kit.outwait.server.slot.SlotCode
import edu.kit.outwait.server.socketHelper.SocketFacade

/**
 *  This class manages all client-Objects
 *      (f. ex. adds SlotInformationReceivers of client to DatabaseWrapper/UpdateMediator).
 *  Encapsulates all communication from Client-Objects to other Classes (f.ex. DatabaseWrapper).
 *  @param namespace forwards to constructor of AbstractManager where it is configured
 *  @param databaseWrapper reference to databaseWrapper so SlotInformationReceivers can be added
 *  @property clients saves reference to objects of active(connected) client connections
 */
class ClientManager(namespace: SocketIONamespace, databaseWrapper: DatabaseWrapper) :
    AbstractManager(namespace, databaseWrapper) {

    private val clients = mutableListOf<Client>()


    /**
     * Collection of all relevant events for client-namespace are passed to configureEventListeners-method
     * in super class.
     */
    init {
        val eventList = listOf<Event>(Event.LISTEN_SLOT, Event.REFRESH_SLOT_APPROX)
        super.configureEventListeners(eventList)
        println("CLIENT-MGR: Client manager initialized")
    }

    /**
     * Implementation of abstract bindSocket-method of super class.
     * Called by configureConnectionCreation-method of super class in onConnection-event.
     * Creates a client object for incoming connection and adds it to list.
     * TODO: Überprüfung ob SocketIOClient-Parameter nötig?
     * @param socket
     * @param socketFacade passed to new client-Object
     */
    override fun bindSocket(socket: SocketIOClient, socketFacade: SocketFacade) {
        println("CLIENT-MGR: Binding new socket")
        val client = Client(socketFacade, this)
        clients.add(client)
    }

    /**
     * Removes client-Object from clients list (f.ex. in case of disconnect-Event of a client).
     *
     * @param client client-Object to remove-
     */
    fun removeClient(client: Client) {
        clients.remove(client)
        println("CLIENT-MGR: Client removed")
    }

    /**
     * Registers a receiver from Client at DatabaseWrapper.
     * @return false if slotCode is invalid else true.
     */
    fun registerReceiver(slotCode: SlotCode, receiver: SlotInformationReceiver): Boolean {
        println("CLIENT-MGR: Register new receiver")
        return this.databaseWrapper.registerReceiver(receiver, slotCode)
    }

    /**
     * Removes a receiver of Client
     */
    fun removeReceiver(receiver: SlotInformationReceiver) {
        println("CLIENT-MGR: Remove receiver")
        this.databaseWrapper.unregisterReceiver(receiver)
    }
}
