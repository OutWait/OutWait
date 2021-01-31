package elite.kit.outwait.waitingQueue.timeSlotModel


class SpontaneousTimeSlot(interval: Long, var slotCode:String,var auxiliaryIdentifier:String) :TimeSlot(interval) {
    override fun getType(): Int {
        return Type.SPONTANEOUS_SLOT.value
    }
}
