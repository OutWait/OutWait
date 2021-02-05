package edu.kit.outwait.server.client

import edu.kit.outwait.server.management.SlotManagementInformation
import edu.kit.outwait.server.protocol.Event
import edu.kit.outwait.server.protocol.JSONEmptyWrapper
import edu.kit.outwait.server.protocol.JSONObjectWrapper
import edu.kit.outwait.server.protocol.JSONSlotCodeWrapper
import edu.kit.outwait.server.slot.SlotCode
import edu.kit.outwait.server.socketHelper.SocketFacade
import java.util.Date

/**
 *  Each Client-Object represents an incoming connection on the client-namespace
 *  Configuration of eventListeners and send of events through personal SocketFacade
 *  @param socketFacade configuration and sending of event(listener)
 *  @param clientManager used to communicate with other packages f.ex. core-package to register a SlotInformationReceiver
 *      and also to remove itself.
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




    private fun configureReceives() {
        socketFacade.onReceive(Event.LISTEN_SLOT, {receivedData ->
            val slotCode = (receivedData as JSONSlotCodeWrapper).getSlotCode()
            addSlot(slotCode)
        })

        socketFacade.onReceive(Event.REFRESH_SLOT_APPROX, {receivedData ->
            val slotCode = (receivedData as JSONSlotCodeWrapper).getSlotCode()
            val slotApprox = receivers.get(slotCode)!!.getSlotApprox()
            this.sendSlotApprox(slotCode, slotApprox)
        })

    }

    private fun addSlot(slotCode: SlotCode) {
    }

    private fun removeSlot(slotCode: SlotCode) {
    }

    fun endSlot(slotCode: SlotCode) {
    }

    fun deleteSlot(slotCode: SlotCode) {
    }

    fun sendSlotApprox(slotCode: SlotCode, slotApprox: Date) {
    }

    fun sendManagementInformation(
        slotCode: SlotCode,
        slotManagementInformation: SlotManagementInformation
    ) {
    }
}
