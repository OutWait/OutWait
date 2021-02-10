package elite.kit.outwait.waitingQueue.timeSlotModel

import org.joda.time.Interval


class SpontaneousTimeSlot(
    interval: Interval,
    slotCode: String,
    auxiliaryIdentifier: String
) : ClientTimeSlot(interval, slotCode, auxiliaryIdentifier) {

    override fun getType(): Type {
        return Type.SPONTANEOUS_SLOT
    }
}
