package elite.kit.outwait.waitingQueue.timeSlotModel

class Pause(interval: Long) : TimeSlot(interval) {
    override fun getType(): Int {
        return Type.PAUSE.value
    }
}
