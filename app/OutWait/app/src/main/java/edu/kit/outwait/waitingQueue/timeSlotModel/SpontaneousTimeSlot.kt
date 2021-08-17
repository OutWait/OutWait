package edu.kit.outwait.waitingQueue.timeSlotModel

import org.joda.time.Interval

/**
 * This class represents a client time slot with its currently predicted time interval
 * for a client who has not a fixed appointment.
 *
 * @constructor
 * Creates SpontaneousTimeSlot with given [slotCode], [auxiliaryIdentifier] and [interval]
 *
 * @param interval see [TimeSlot.interval]
 * @param slotCode see [ClientTimeSlot.slotCode]
 * @param auxiliaryIdentifier see [ClientTimeSlot.auxiliaryIdentifier]
 */
class SpontaneousTimeSlot(
    interval: Interval,
    slotCode: String,
    auxiliaryIdentifier: String
) : ClientTimeSlot(interval, slotCode, auxiliaryIdentifier) {

    override fun getType(): Type {
        return Type.SPONTANEOUS_SLOT
    }
}
