package edu.kit.outwait.waitingQueue.timeSlotModel

import org.joda.time.Interval

/**
 * This class represents a time interval where, corresponding to the current
 * approximation, no appointment or other kind of slot takes place.
 *
 * @constructor
 * creates a Pause in the given time [interval]
 *
 * @param interval see [TimeSlot.interval]
 */
class Pause(interval: Interval) : TimeSlot(interval) {
    override fun getType(): Type {
        return Type.PAUSE
    }
}
