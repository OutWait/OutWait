package elite.kit.outwait.waitingQueue.timeSlotModel

class FixedTimeSlot(interval: Long,var slotCode:String,var auxiliaryIdentifier:String, var appointmentTime:Long) :TimeSlot(interval) {
    override fun getType(): Int {
        return Type.FIXED_SLOT.value
    }
}
