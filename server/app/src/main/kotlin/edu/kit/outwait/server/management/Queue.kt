package edu.kit.outwait.server.management

import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.slot.Slot
import edu.kit.outwait.server.slot.SlotCode
import java.time.Duration
import java.util.Date

class Queue(
    val managementId: ManagementId,
    val queueId: QueueId,
    databaseWrapper: DatabaseWrapper
) {
    private var slots = mutableListOf<Slot>()

    init {
        slots = databaseWrapper.getSlots(queueId).toMutableList()
    }
    fun updateQueue(prioritizationTime: Duration) {
        // TODO
    }
    fun calculateNextDelayChange(): Date {
        // TODO
        return Date()
    }
    fun storeToDB(databaseWrapper: DatabaseWrapper) {
        databaseWrapper.saveSlots(slots, queueId)
    }
    fun addSpontaneousSlot(slot: Slot) {
        slots.add(slot);
    }
    fun addFixedSlot(slot: Slot) {
        slots.add(slot);
    }
    fun deleteSlot(slotCode: SlotCode) {
        slots.removeIf({ it.slotCode == slotCode })
    }
    fun endCurrentSlot() {
        if(slots.isNotEmpty()) {
            slots.removeAt(0)
        }
    }
    fun moveSlotAfterAnother(slotToMove: SlotCode, otherSlot: SlotCode) {
        val slot = slots.find { it.slotCode == slotToMove }
        if (slot != null) {
            slots.remove(slot)
            val targetIndex = slots.indexOfFirst { it.slotCode == otherSlot }
            slots.add(targetIndex + 1, slot)
        }
    }

    /** Replaces a slot in the list with a updated slot */
    private fun replaceSlot(oldSlot:Slot, newSlot:Slot) {
        val index = slots.indexOf(oldSlot)
        slots.remove(oldSlot)
        slots.add(index, newSlot)
    }

    fun changeAppointmentTime(slotCode: SlotCode, newTime: Date) {
        var oldSlot = slots.find { it.slotCode == slotCode }
        if (oldSlot != null) {
            replaceSlot(oldSlot, oldSlot.copy(constructorTime=newTime))
        }
    }
    fun updateSlotLength(slotCode: SlotCode, newLength: Duration) {
        var oldSlot = slots.find { it.slotCode == slotCode }
        if (oldSlot != null) {
            replaceSlot(oldSlot, oldSlot.copy(expectedDuration=newLength))
        }
    }
}
