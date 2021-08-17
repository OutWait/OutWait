package edu.kit.outwait.waitingQueue.gravityQueue

import edu.kit.outwait.waitingQueue.timeSlotModel.SpontaneousTimeSlot
import edu.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval

/**
 * This class is models a client time slot with gravity characteristics (see [GravitySlot])
 * for a client who has not a fixed appointment
 * The class implements the [TimeSlot.interval] method with the specific gravity behaviour of
 * spontaneous time slots: They start immediately when it is possible.
 *
 * @constructor
 * Creates SpontaneousGravitySlot with given [slotCode], [auxiliaryIdentifier] and [duration]
 *
 * @param slotCode see [ClientGravitySlot.slotCode]
 * @param duration see [GravitySlot.duration]
 * @param auxiliaryIdentifier see [ClientGravitySlot.auxiliaryIdentifier]
 */
class SpontaneousGravitySlot(
    slotCode: String,
    duration: Duration,
    auxiliaryIdentifier: String
) : ClientGravitySlot(slotCode, duration, auxiliaryIdentifier) {

    override fun toClientTimeSlot(predecessor: TimeSlot): TimeSlot {
        val interval = interval(predecessor.interval.end)
        return SpontaneousTimeSlot(interval, slotCode, auxiliaryIdentifier)
    }

    override fun interval(earliestPossibleStart: DateTime): Interval {
        return Interval(earliestPossibleStart, earliestPossibleStart + duration)
    }
}
