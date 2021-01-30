package elite.kit.outwait.waitingQueue.timeSlotModel

enum class Type(var value:Int) {
    PAUSE(0),
    FIXED_SLOT(1),
    SPONTANEOUS_SLOT(2),
    DEFAULT(3)
}
