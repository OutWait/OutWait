package edu.kit.outwait.server.core

import edu.kit.outwait.server.client.SlotInformationReceiver
import edu.kit.outwait.server.management.SlotManagementInformation
import edu.kit.outwait.server.slot.SlotCode
import java.util.Date

class UpdateMediator {
    private val receivers = hashMapOf<SlotCode, MutableSet<SlotInformationReceiver>>()

    fun subscribeReceiver(
        receiver: SlotInformationReceiver,
        slotCode: SlotCode,
        slotApprox: Date,
        slotManagementInformation: SlotManagementInformation
    ) {
        receivers.getOrPut(slotCode) { mutableSetOf<SlotInformationReceiver>() }.add(receiver)

        // Update information
        setManagementInformation(listOf(slotCode), slotManagementInformation)
        setSlotApprox(slotCode, slotApprox)
    }

    fun unsubscribeSlotInformationReceiver(slotCode: SlotCode, receiver: SlotInformationReceiver) {
        if (receivers[slotCode] != null) {
            receivers[slotCode]?.remove(receiver)
            if (receivers[slotCode]?.isEmpty() ?: false) {
                // Remove empty set
                receivers.remove(slotCode)
            }
        }
    }

    fun setSlotApprox(slotCode: SlotCode, slotApprox: Date) {
        receivers[slotCode]?.forEach() { it.setSlotApprox(slotApprox) }
            ?: println("Unknown slot requested (" + slotCode + ") in UpdateMediator")
    }

    fun setManagementInformation(
        slotCodes: List<SlotCode>,
        slotManagementInformation: SlotManagementInformation
    ) {
        for ( code in slotCodes ) {
            receivers[code]
                ?.forEach() { it.setSlotManagementInformation(slotManagementInformation) }
                ?: println("Unknown slot requested (" + code + ") in UpdateMediator")
        }
    }

    fun endSlot(slotCode: SlotCode) {
        receivers[slotCode]?.forEach() { it.end() }
            ?: println("Unknown slot requested (" + slotCode + ") in UpdateMediator")
    }

    fun deleteSlot(slotCode: SlotCode) {
        receivers[slotCode]?.forEach() { it.delete() }
            ?: println("Unknown slot requested (" + slotCode + ") in UpdateMediator")
    }
}
