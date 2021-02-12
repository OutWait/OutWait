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
    }

    /**
     * Configuration of onReceives through SocketFacade.
     * TODO: Lambdas korrekt?
     */
    private fun configureReceives() {
        socketFacade.onReceive(Event.LISTEN_SLOT, {receivedData ->
            val slotCode = (receivedData as JSONSlotCodeWrapper).getSlotCode()
            addSlot(slotCode)
        })

        socketFacade.onReceive(Event.REFRESH_SLOT_APPROX, {receivedData ->
            val slotCode = (receivedData as JSONSlotCodeWrapper).getSlotCode()
            if (receivers[slotCode] == null) {
                this.socketFacade.send(Event.INVALID_CODE, JSONEmptyWrapper())
            }
            else {
                val slotApprox = receivers.get(slotCode)!!.getSlotApprox()
                this.sendSlotApprox(slotCode, slotApprox)
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
            receivers[slotCode] = slotInformationReceiver
        }
        else {
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
        if (removeSlot(slotCode)) {
            val toSend = JSONSlotCodeWrapper()
            toSend.setSlotCode(slotCode)
            socketFacade.send(Event.SLOT_ENDED, toSend)
        }
        else {
            socketFacade.send(Event.INVALID_CODE, JSONEmptyWrapper())
        }
    }

    /**
     * Called if a Slot deleted
     * @param slotCode Deleted Slot
     */
    fun deleteSlot(slotCode: SlotCode) {
        if (removeSlot(slotCode)) {
            val toSend = JSONSlotCodeWrapper()
            toSend.setSlotCode(slotCode)
            socketFacade.send(Event.SLOT_DELETED, toSend)
        }
        else {
            socketFacade.send(Event.INVALID_CODE, JSONEmptyWrapper())
        }
    }

    /**
     * Send slotApprox to Client for a specific Slot
     * Called for updates or initialization of the EAT of a Slot
     * @param slotApprox ETA of Slot
     * @param slotCode SlotCode of Slot
     */
    fun sendSlotApprox(slotCode: SlotCode, slotApprox: Date) {
        val toSend = JSONSlotApproxWrapper()
        toSend.setSlotApprox(slotApprox)
        toSend.setSlotCode(slotCode)
        socketFacade.send(Event.SEND_SLOT_APPROX, toSend)
    }

    /**
     * Send Management Information to Client for a specific Slot
     * Called for updates or initialization of management information
     * @param slotManagementInformation Management Information of Slot
     * @param slotCode SlotCode of Slot
     */
    fun sendManagementInformation(
        slotCode: SlotCode,
        slotManagementInformation: SlotManagementInformation
    ) {
        val toSend = JSONSlotManagementInformationWrapper()
        toSend.setSlotCode(slotCode)
        toSend.setInformation(slotManagementInformation)
        socketFacade.send(Event.UPDATE_MANAGEMENT_INFORMATION, toSend)
    }
}
