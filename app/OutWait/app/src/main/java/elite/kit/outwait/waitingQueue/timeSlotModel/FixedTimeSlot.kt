package elite.kit.outwait.waitingQueue.timeSlotModel

import org.joda.time.DateTime
import org.joda.time.Interval

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
