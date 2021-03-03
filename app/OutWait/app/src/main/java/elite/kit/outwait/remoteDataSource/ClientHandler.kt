package elite.kit.outwait.remoteDataSource

import androidx.lifecycle.LiveData

/**
 * This interface describes all methods for network communication, that the
 * client repository (or higher tier) can use to send to and receive data from the server.
 * They must be provided by the respective implementation of the network communication
 *
 */
interface ClientHandler {

    /**
     * This method sets up the communication resources, depending on the used
     * implementation of client-server communication (e.g WebSockets, REST etc)
     *
     * @return true, if communication was successfully established, else false
     */
    fun initCommunication(): Boolean

    /**
     * This method releases all communication resources, depending on the used
     * implementation of client-server communication (e.g WebSockets, REST etc)
     *
     * @return true, if communication was successfully released, else false
     */
    fun endCommunication(): Boolean

    /**
     * This method emits the event "listenSlot@S" with the given slotCode as data to the server
     *
     * @param slotCode specifies the slot that the client wants to observe
     */
    fun newCodeEntered(slotCode: String)

    /**
     * This method emits the event "refreshSlotApprox@S" with the given slotCode as data
     *
     * @param slotCode specifies the slot for which the waiting time refresh was requested
     */
    fun refreshWaitingTime(slotCode: String)

    /**
     * Getter for LiveData, that provides the client repository (or higher tier)
     * with error messages, that may be time-displaced or unexpectedly received by
     * the server
     *
     * @return list of ClientServerErrors, as LiveData
     */
    fun getErrors() : LiveData<List<ClientServerErrors>>
}
