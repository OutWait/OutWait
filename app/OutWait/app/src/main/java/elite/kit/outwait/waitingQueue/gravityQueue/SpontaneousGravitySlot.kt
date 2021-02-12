package elite.kit.outwait.waitingQueue.gravityQueue

import elite.kit.outwait.waitingQueue.timeSlotModel.SpontaneousTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval

class SpontaneousGravitySlot(slotCode: String, duration: Duration) : ClientGravitySlot(slotCode, duration) {
    override fun toClientTimeSlot(predecessor: TimeSlot): TimeSlot {
        val interval = interval(predecessor.interval.end)
        return SpontaneousTimeSlot(interval, slotCode, auxiliaryIdentifier)
    }

    override fun interval(scheduledStart: DateTime): Interval {
        return Interval(scheduledStart, scheduledStart + duration)
    }
}
