package edu.kit.outwait.server.client

import java.util.Date

import edu.kit.outwait.server.slot.SlotCode
import edu.kit.outwait.server.socketHelper.SocketFacade
import edu.kit.outwait.server.management.SlotManagementInformation

class Client(private val socketFacade: SocketFacade, private val clientManager: ClientManager) {
    private val receivers = hashMapOf<SlotCode, SlotInformationReceiver>()

    init {

    }

    private fun configureReceives() {

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

    fun sendManagementInformation(slotCode: SlotCode, slotManagementInformation: SlotManagementInformation) {

    }

}
