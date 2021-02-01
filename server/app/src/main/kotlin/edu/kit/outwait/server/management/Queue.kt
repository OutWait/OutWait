package edu.kit.outwait.management

class Queue(managementId: ManagementId, queueId: QueueId, databaseWrapper: DatabaseWrapper) {
    private queueId: QueueId
    private managementId: ManagementId
    private slots: List<Slot>

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
