package edu.kit.outwait.server.core

import edu.kit.outwait.server.client.SlotInformationReceiver
import edu.kit.outwait.server.management.SlotManagementInformation
import edu.kit.outwait.server.slot.SlotCode
import java.util.Date

/**
 *  This class represents a mediator object between (changes made in the) Databasewrapper and (SlotInformation)receivers.
 *  (SlotInformation)receivers have to be registered and unregistered.
 *  @property receivers reference to SlotInformationReceivers to call methods in case of f. ex. changes.
 */
class UpdateMediator {
    private val receivers = hashMapOf<SlotCode, MutableSet<SlotInformationReceiver>>()

    /**
     * Adds a SlotInformationReceiver to slots and sends data of Slot to SlotInformationReceiver.
     * Called by DatabaseWrapper with Slot data.
     * @param receiver SlotInformationReceiver to be added.
     * @param slotCode Slot Code of SlotInformationReceivers Slot. Needed to identify SlotInformationReceiver in
     * receivers
     * @param slotApprox Approximated Time of SlotInformationReceivers Slot.
     * @param slotManagementInformation ManagementInformation about SlotInformationReceivers Slot.
     */
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

    /**
     * Removes a SlotInformationReceiver from slots.
     * @param receiver SlotInformationReceiver to be removed.
     */
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
     * will only send one message to the client. Combines both.
     * @param slotCode Slot Code of SlotInformationReceivers Slot. Needed to identify SlotInformationReceiver in
     * receivers
     * @param slotApprox Approximated Time of SlotInformationReceivers Slot.
     * @param slotManagementInformation ManagementInformation about SlotInformationReceivers Slot.
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

    /**
     * Backup method to set SlotApprox in SlotInformationReceiver if needed.
     * @param slotCode Slot Code of SlotInformationReceivers Slot. Needed to identify SlotInformationReceiver in
     * receivers
     * @param slotApprox Approximated Time of SlotInformationReceivers Slot.
     */
    fun setSlotApprox(slotCode: SlotCode, slotApprox: Date) {
        println("UP-MEDI: Update slot approx of slot " + slotCode + " to " + slotApprox)
        receivers[slotCode]
            ?.forEach() { it.setSlotData(slotApprox, it.getSlotManagementInformation()) }
            ?: println("Unknown slot requested (" + slotCode + ") in UpdateMediator")
    }

    /**
     * Sets SlotManagementInformation for a list of SlotInformationReceivers.
     * @param slotCodes Slot Codes of SlotInformationReceivers Slot. Needed to identify SlotInformationReceiver in
     * receivers
     * @param slotManagementInformation ManagementInformation about SlotInformationReceivers Slot.
     */
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

    /**
     * Calls endSlot on SlotInformationReceiver.
     * @param slotCode Slot Code of SlotInformationReceivers Slot. Needed to identify SlotInformationReceiver in
     * receivers
     */
    fun endSlot(slotCode: SlotCode) {
        println("UP-MEDI: Ending slot " + slotCode)
        receivers[slotCode]?.forEach() { it.end() }
            ?: println("Unknown slot requested (" + slotCode + ") in UpdateMediator")
    }

    /**
     * Calls deleteSlot on SlotInformationReceiver.
     * @param slotCode Slot Code of SlotInformationReceivers Slot. Needed to identify SlotInformationReceiver in
     * receivers
     */
    fun deleteSlot(slotCode: SlotCode) {
        println("UP-MEDI: Deleting slot " + slotCode)
        receivers[slotCode]?.forEach() { it.delete() }
            ?: println("Unknown slot requested (" + slotCode + ") in UpdateMediator")
    }
}
