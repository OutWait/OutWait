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
        println("UP-MEDI: Subscribing new receiver for code " + slotCode)
        receivers.getOrPut(slotCode) { mutableSetOf<SlotInformationReceiver>() }.add(receiver)

        // Update information
        setSlotData(slotCode, slotApprox, slotManagementInformation)
    }

    fun unsubscribeSlotInformationReceiver(receiver: SlotInformationReceiver) {
        println("UP-MEDI: Unsubscribing receiver for code " + receiver.slotCode)
        val slotCode = receiver.slotCode
        if (receivers[slotCode] != null) {
            receivers[slotCode]?.remove(receiver)
            if (receivers[slotCode]?.isEmpty() ?: false) {
                // Remove empty set
                receivers.remove(slotCode)
            }
        } else {
            println("UP-MEDI: Failed to unsubscribe (not subscribed jet)")
        }
    }

    /**
     * Unlike successive calls to setSlotApprox and setManagementInformation, calling this method
     * will only send one message to the client.
     */
    fun setSlotData(
        slotCode: SlotCode,
        slotApprox: Date,
        slotManagementInformation: SlotManagementInformation
    ) {
        println(
            "UP-MEDI: Update slot data of slot " + slotCode + " to " + slotApprox + " and info " +
                slotManagementInformation
        )
        receivers[slotCode]?.forEach() { it.setSlotData(slotApprox, slotManagementInformation) }
            ?: println("Unknown slot requested (" + slotCode + ") in UpdateMediator")
    }

    fun setSlotApprox(slotCode: SlotCode, slotApprox: Date) {
        println("UP-MEDI: Update slot approx of slot " + slotCode + " to " + slotApprox)
        receivers[slotCode]
            ?.forEach() { it.setSlotData(slotApprox, it.getSlotManagementInformation()) }
            ?: println("Unknown slot requested (" + slotCode + ") in UpdateMediator")
    }

    fun setManagementInformation(
        slotCodes: List<SlotCode>,
        slotManagementInformation: SlotManagementInformation
    ) {
        for (code in slotCodes) {
            println(
                "UP-MEDI: Update slot info of slot " + code + " to " + slotManagementInformation
            )
            receivers[code]
                ?.forEach() { it.setSlotData(it.getSlotApprox(), slotManagementInformation) }
                ?: println("Unknown slot requested (" + code + ") in UpdateMediator")
        }
    }

    fun endSlot(slotCode: SlotCode) {
        println("UP-MEDI: Ending slot " + slotCode)
        receivers[slotCode]?.forEach() { it.end() }
            ?: println("Unknown slot requested (" + slotCode + ") in UpdateMediator")
    }

    fun deleteSlot(slotCode: SlotCode) {
        println("UP-MEDI: Deleting slot " + slotCode)
        receivers[slotCode]?.forEach() { it.delete() }
            ?: println("Unknown slot requested (" + slotCode + ") in UpdateMediator")
    }
}
