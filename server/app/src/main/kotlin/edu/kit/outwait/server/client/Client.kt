package edu.kit.outwait.server.client

import edu.kit.outwait.server.core.Logger
import edu.kit.outwait.server.management.SlotManagementInformation
import edu.kit.outwait.server.protocol.*
import edu.kit.outwait.server.slot.SlotCode
import edu.kit.outwait.server.socketHelper.SocketFacade
import java.util.Date

/**
 * Each Client-Object represents an incoming connection on the client-namespace.
 *
 * Configuration of eventListeners and send of events through personal SocketFacade.
 *
 * @param socketFacade configuration and sending of event(listener)
 * @param clientManager used to communicate with other packages f.ex. core-package to register a
 *     SlotInformationReceiver and also to remove itself.
 * @property receivers maps SlotCode to his SlotInformationReceiver
 * @constructor Configure EventListeners and send READY_TO_SERVE-Event so that client on app
 *     acknowledges that Client can now listen to events.
 */
class Client(private val socketFacade: SocketFacade, private val clientManager: ClientManager) {
    private val receivers = hashMapOf<SlotCode, SlotInformationReceiver>()
    private val LOG_ID = "CLIENT"

    init {
        this.configureReceives()
        socketFacade.send(Event.READY_TO_SERVE, JSONEmptyWrapper())
        Logger.debug(LOG_ID, "Initialized")
    }

    /** Configuration of onReceives through SocketFacade. */
    private fun configureReceives() {
        socketFacade.onReceive(Event.LISTEN_SLOT) { receivedData ->
            val slotCode = (receivedData as JSONSlotCodeWrapper).getSlotCode()
            Logger.debug(LOG_ID, "Listen to slot code $slotCode")
            addSlot(slotCode)
        }

        socketFacade.onReceive(Event.REFRESH_SLOT_APPROX) { receivedData ->
            val slotCode = (receivedData as JSONSlotCodeWrapper).getSlotCode()
            Logger.debug(LOG_ID, "Refresh slot approx manually, code $slotCode")
            if (receivers[slotCode] == null) {
                Logger.debug(LOG_ID, "Slot code to refresh was not registered before")
                this.socketFacade.send(Event.INVALID_CLIENT_REQUEST, JSONEmptyWrapper())
            } else {
                val slotApprox = receivers.get(slotCode)!!.getSlotApprox()
                val slotManagementInformation =
                    receivers.get(slotCode)!!.getSlotManagementInformation()
                this.sendSlotData(slotCode, slotApprox, slotManagementInformation)
            }
        }

        socketFacade.onDisconnect {
            for ((_, receiver) in receivers) {
                clientManager.removeReceiver(receiver)
            }
            clientManager.removeClient(this)
        }
    }

    /**
     *  Adds a new SlotCode to receivers. Called when a client wants to listen to a new Slot. @param
     * slotCode Slot to listen
     */
    private fun addSlot(slotCode: SlotCode) {
        val slotInformationReceiver = SlotInformationReceiver(this, slotCode)
        if (clientManager.registerReceiver(slotCode, slotInformationReceiver)) {
            Logger.debug(LOG_ID, "Adding slot code " + slotCode + " to receiver list")
            receivers[slotCode] = slotInformationReceiver
        } else {
            Logger.debug(LOG_ID, "Slot does not exist (can't add it to receiver list)")
            val toSend = JSONSlotCodeWrapper()
            toSend.setSlotCode(slotCode)
            socketFacade.send(Event.INVALID_CODE, toSend)
        }
    }

    /**
     *  Removes reference to SlotInformationReceivers of a SlotCode Calls removeReceiver-method on
     * ClientManager. Called by endSlot and deleteSlot-method @param slotCode Slot to remove
     */
    private fun removeSlot(slotCode: SlotCode) : Boolean {
        Logger.debug(LOG_ID, "Removing slot " + slotCode + " from receiver list")
        val slotInformationReceiver = receivers[slotCode] ?: return false
        clientManager.removeReceiver(slotInformationReceiver)
        receivers.remove(slotCode)
        return true
    }

    /**
     * Called if a Slot ended
     *
     * @param slotCode Ended Slot
     */
    fun endSlot(slotCode: SlotCode) {
        Logger.debug(LOG_ID, "End slot...")
        if (removeSlot(slotCode)) {
            val toSend = JSONSlotCodeWrapper()
            toSend.setSlotCode(slotCode)
            socketFacade.send(Event.SLOT_ENDED, toSend)
            Logger.debug(LOG_ID, "Slot ended")
        } else {
            Logger.debug(LOG_ID, "Could not end slot (code not registered)")
            val toSend = JSONSlotCodeWrapper()
            toSend.setSlotCode(slotCode)
            socketFacade.send(Event.INVALID_CODE, toSend)
        }
    }

    /**
     * Called if a Slot deleted
     *
     * @param slotCode Deleted Slot
     */
    fun deleteSlot(slotCode: SlotCode) {
        Logger.debug(LOG_ID, "Delete slot...")
        if (removeSlot(slotCode)) {
            val toSend = JSONSlotCodeWrapper()
            toSend.setSlotCode(slotCode)
            socketFacade.send(Event.SLOT_DELETED, toSend)
            Logger.debug(LOG_ID, "Slot deleted")
        } else {
            Logger.debug(LOG_ID, "Could not delete slot (code not registered)")
            val toSend = JSONSlotCodeWrapper()
            toSend.setSlotCode(slotCode)
            socketFacade.send(Event.INVALID_CODE, toSend)
        }
    }

    /**
     * Send slotApprox and Management Information to Client for a specific Slot Called for updates
     * or initialization of a Slot
     *
     * @param slotApprox ETA of Slot
     * @param slotCode SlotCode of Slot
     * @param slotManagementInformation Management Information of Slot
     */
    fun sendSlotData(
        slotCode: SlotCode,
        slotApprox: Date,
        slotManagementInformation: SlotManagementInformation
    ) {
        Logger.debug(
            LOG_ID,
            "Sending updated slot data  " + slotApprox + " and settings " +
                slotManagementInformation + " for slot " + slotCode
        )
        val toSend = JSONSlotDataWrapper()
        toSend.setSlotApprox(slotApprox)
        toSend.setSlotCode(slotCode)
        toSend.setInformation(slotManagementInformation)
        socketFacade.send(Event.SEND_SLOT_DATA, toSend)
    }
}
