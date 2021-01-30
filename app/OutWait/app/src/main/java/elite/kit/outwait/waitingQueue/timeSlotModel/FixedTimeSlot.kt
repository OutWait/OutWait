package elite.kit.outwait.waitingQueue.timeSlotModel

class FixedTimeSlot(interval: Long) :TimeSlot(interval) {
    override fun getType(): Int {
        return Type.FIXED_SLOT.value
    }
}
