package edu.kit.outwait.server.client

import edu.kit.outwait.server.management.SlotManagementInformation
import edu.kit.outwait.server.protocol.*
import edu.kit.outwait.server.slot.SlotCode
import edu.kit.outwait.server.socketHelper.SocketFacade
import java.util.Date

/**
 *  Each Client-Object represents an incoming connection on the client-namespace
 *  Configuration of eventListeners and send of events through personal SocketFacade
 *  @param socketFacade configuration and sending of event(listener)
 *  @param clientManager used to communicate with other packages f.ex. core-package to register a SlotInformationReceiver
 *   and also to remove itself.
 *  @property receivers maps SlotCode to his SlotInformationReceiver
 */
class Client(private val socketFacade: SocketFacade, private val clientManager: ClientManager) {
    private val receivers = hashMapOf<SlotCode, SlotInformationReceiver>()

    /**
     *  Configure EventListeners and send READY_TO_SERVE-Event so that client on app acknowledges that
     *  Client can now listen to events.
     */
    init {
        this.configureReceives()
        socketFacade.send(Event.READY_TO_SERVE, JSONEmptyWrapper())
        println("CLIENT: initialized")
    }

    /**
     * Configuration of onReceives through SocketFacade.
     */
    private fun configureReceives() {
        socketFacade.onReceive(Event.LISTEN_SLOT, {receivedData ->
            val slotCode = (receivedData as JSONSlotCodeWrapper).getSlotCode()
            println("CLIENT: Listen to slot code "+slotCode)
            addSlot(slotCode)
        })

        socketFacade.onReceive(Event.REFRESH_SLOT_APPROX, {receivedData ->
            val slotCode = (receivedData as JSONSlotCodeWrapper).getSlotCode()
            println("CLIENT: Refresh slot approx manually, code " + slotCode)
            if (receivers[slotCode] == null) {
            println("CLIENT: Slot code to refresh was not registered before")
                this.socketFacade.send(Event.INVALID_CLIENT_REQUEST, JSONEmptyWrapper())
            }
            else {
                val slotApprox = receivers.get(slotCode)!!.getSlotApprox()
                val slotManagementInformation = receivers.get(slotCode)!!.getSlotManagementInformation()
                this.sendSlotData(slotCode, slotApprox, slotManagementInformation)
            }
        })
    }

    /**
     *  Adds a new SlotCode to receivers. Called when a client wants to listen to a new Slot.
     *  @param slotCode Slot to listen
     */
    private fun addSlot(slotCode: SlotCode) {
        val slotInformationReceiver = SlotInformationReceiver(this, slotCode)
        if (clientManager.registerReceiver(slotCode, slotInformationReceiver)) {
            println("CLIENT: Adding slot code "+slotCode+ " to receiver list")
            receivers[slotCode] = slotInformationReceiver
        }
        else {
            println("CLIENT: Slot does not exist (can't add it to receiver list)")
            socketFacade.send(Event.INVALID_CODE, JSONEmptyWrapper())
        }
    }

    /**
     *  Removes reference to SlotInformationReceivers of a SlotCode
     *  Calls removeReceiver-method on ClientManager.
     *  Calles by endSlot and deleteSlot-method
     *  @param slotCode Slot to remove
     */
    private fun removeSlot(slotCode: SlotCode) : Boolean {
        println("CLIENT: Removing slot " + slotCode+ " from receiver list")
        val slotInformationReceiver = receivers[slotCode] ?: return false
        clientManager.removeReceiver(slotInformationReceiver)
        receivers.remove(slotCode)
        return true
    }

    /**
     * Called if a Slot ended
     * @param slotCode Ended Slot
     */
    fun endSlot(slotCode: SlotCode) {
        println("CLIENT: End slot...")
        if (removeSlot(slotCode)) {
            val toSend = JSONSlotCodeWrapper()
            toSend.setSlotCode(slotCode)
            socketFacade.send(Event.SLOT_ENDED, toSend)
            println("CLIENT: Slot ended")
        }
        else {
        println("CLIENT: Could not end slot (code not registered)")
            socketFacade.send(Event.INVALID_CODE, JSONEmptyWrapper())
        }
    }

    /**
     * Called if a Slot deleted
     * @param slotCode Deleted Slot
     */
    fun deleteSlot(slotCode: SlotCode) {
        println("CLIENT: Delete slot...")
        if (removeSlot(slotCode)) {
            val toSend = JSONSlotCodeWrapper()
            toSend.setSlotCode(slotCode)
            socketFacade.send(Event.SLOT_DELETED, toSend)
        println("CLIENT: Slot deleted")
        }
        else {
        println("CLIENT: Could not delete slot (code not registered)")
            socketFacade.send(Event.INVALID_CODE, JSONEmptyWrapper())
        }
    }

    /**
     * Send slotApprox and Management Information to Client for a specific Slot
     * Called for updates or initialization of a Slot
     * @param slotApprox ETA of Slot
     * @param slotCode SlotCode of Slot
     * @param slotManagementInformation Management Information of Slot
     */
    fun sendSlotData(slotCode: SlotCode, slotApprox: Date, slotManagementInformation: SlotManagementInformation) {
        println("CLIENT: Sending updated slot data  "+slotApprox + " and settings " + slotManagementInformation + " for slot "+slotCode)
        val toSend = JSONSlotDataWrapper()
        toSend.setSlotApprox(slotApprox)
        toSend.setSlotCode(slotCode)
        toSend.setInformation(slotManagementInformation)
        socketFacade.send(Event.SEND_SLOT_DATA, toSend)
    }
}
