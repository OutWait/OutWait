package elite.kit.outwait.waitingQueue.timeSlotModel

import org.joda.time.Interval

class Pause(interval: Interval) : TimeSlot(interval) {
    override fun getType(): Type {
        return Type.PAUSE
    }

}
