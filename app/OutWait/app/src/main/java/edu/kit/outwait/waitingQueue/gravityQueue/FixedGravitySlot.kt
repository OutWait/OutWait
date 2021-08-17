package edu.kit.outwait.waitingQueue.gravityQueue

import edu.kit.outwait.waitingQueue.timeSlotModel.FixedTimeSlot
import edu.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval

/**
 * This class is models a client time slot with gravity characteristics (see [GravitySlot])
 * for a client who has a fixed appointment
 * Additionally to the [ClientGravitySlot] properties it stores the appointments
 * point in time.
 * The class implements the [TimeSlot.interval] method with the specific gravity behaviour of
 * fixed time slots: They cannot start before their [appointmentTime], even if
 * the earliest possible Start is earlier.
 *
 *
 * @property appointmentTime originally arranged point in time of the appointment
 * @constructor
 * Creates FixedGravitySlot with given [slotCode], [duration], [appointmentTime]
 * and [auxiliaryIdentifier].
 *
 * @param slotCode see [ClientGravitySlot.slotCode]
 * @param duration see [GravitySlot.duration]
 * @param auxiliaryIdentifier see [ClientGravitySlot.auxiliaryIdentifier]
 */
class FixedGravitySlot(
    slotCode: String,
    duration: Duration,
    val appointmentTime: DateTime,
    auxiliaryIdentifier: String,
) : ClientGravitySlot(slotCode, duration, auxiliaryIdentifier) {

    override fun toClientTimeSlot(predecessor: TimeSlot): TimeSlot {
        val interval = interval(predecessor.interval.end)
        return FixedTimeSlot(interval, slotCode, auxiliaryIdentifier, appointmentTime)
    }

    override fun interval(earliestPossibleStart: DateTime): Interval {
        val begin: DateTime =
            if (earliestPossibleStart > appointmentTime) {
                earliestPossibleStart
            } else {
                appointmentTime
            }
        val end = begin + duration
        return Interval(begin, end)
    }
}
