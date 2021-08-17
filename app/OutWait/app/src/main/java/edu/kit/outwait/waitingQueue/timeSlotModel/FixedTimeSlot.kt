package edu.kit.outwait.waitingQueue.timeSlotModel

import org.joda.time.DateTime
import org.joda.time.Interval

/**
 * This class represents a client time slot with its currently predicted time interval
 * for a client who has a fixed appointment.
 *
 * @property appointmentTime originally arranged point in time of the appointment
 * @constructor
 * Creates FixedTimeSlot with given [slotCode], [auxiliaryIdentifier], [interval]
 * and [appointmentTime]
 *
 * @param interval see [TimeSlot.interval]
 * @param slotCode see [ClientTimeSlot.slotCode]
 * @param auxiliaryIdentifier see [ClientTimeSlot.auxiliaryIdentifier]
 */
class FixedTimeSlot(
    interval: Interval,
    slotCode: String,
    auxiliaryIdentifier: String,
    val appointmentTime: DateTime
) : ClientTimeSlot(interval, slotCode, auxiliaryIdentifier) {

    override fun getType(): Type {
        return Type.FIXED_SLOT
    }
}
