package elite.kit.outwait.waitingQueue.timeSlotModel


class SpontaneousTimeSlot(interval: Long) :TimeSlot(interval) {
    override fun getType(): Int {
        return Type.SPONTANEOUS_SLOT.value
    }
}
