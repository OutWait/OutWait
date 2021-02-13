package elite.kit.outwait.waitingQueue.gravityQueue

import elite.kit.outwait.waitingQueue.timeSlotModel.FixedTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval

class FixedGravitySlot(slotCode: String, duration: Duration, val appointmentTime: DateTime, auxiliaryIdentifier: String) : ClientGravitySlot(slotCode, duration, auxiliaryIdentifier) {
    override fun toClientTimeSlot(predecessor: TimeSlot): TimeSlot {
        var interval = interval(predecessor.interval.end)
        return FixedTimeSlot(interval, slotCode, auxiliaryIdentifier, appointmentTime)
    }

    override fun interval(scheduledStart: DateTime): Interval {
        val begin: DateTime?
        if (scheduledStart > appointmentTime){
            begin = scheduledStart
        } else {
            begin = appointmentTime
        }
        val end = begin + duration
        return Interval(begin, end)
    }
}
