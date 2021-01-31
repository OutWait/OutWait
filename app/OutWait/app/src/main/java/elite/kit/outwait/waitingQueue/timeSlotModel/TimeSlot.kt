package elite.kit.outwait.waitingQueue.timeSlotModel

abstract class TimeSlot(var interval:Long) {
    abstract fun getType():Int
}
