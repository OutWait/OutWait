package edu.kit.outwait.server.core

import edu.kit.outwait.server.slot.SlotCode
import edu.kit.outwait.server.client.SlotInformationReceiver
import edu.kit.outwait.server.management.SlotManagementInformation

class UpdateMediator {
    private val receivers = hashMapOf<SlotCode, SlotInformationReceiver>()

    init {

    }

    fun subscribeReceiver(receiver : SlotInformationReceiver, slotCode : SlotCode, slotApproxTime : java.util.Date, slotManagementInformation : SlotManagementInformation) {

    }

    fun unsubscribeSlotInformationReceiver(receiver : SlotInformationReceiver) {

    }

    fun setSlotApprox(slotCode: SlotCode, slotApprox: java.util.Date) {

    }

    fun setManagementinformation(slotCodes: List<SlotCode>, slotManagementInformation : SlotManagementInformation) {

    }

    fun endSlot(slotCode: SlotCode) {

    }

    fun deleteSlot(slotCode: SlotCode) {

    }
}
