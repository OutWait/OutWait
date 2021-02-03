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
    private val slots = listOf<Slot>()

    fun updateQueue(prioritizationTime: Duration) {}
    fun calculateNextDelayChange(): Date { return Date() }
    fun storeToDB(databaseWrapper: DatabaseWrapper) {}
    fun addSpontaneousSlot(slot: Slot) {}
    fun addFixedSlot(slot: Slot) {}
    fun deleteSlot(slotCode: SlotCode) {}
    fun endCurrentSlot() {}
    fun moveSlotAfterAnother(slotToMove: SlotCode, otherSlot: SlotCode) {}
    fun changeAppointmentTime(slot: SlotCode, newTime: Date) {}
    fun updateSlotLength(slotCode: SlotCode, newLength: Duration) {}
}
