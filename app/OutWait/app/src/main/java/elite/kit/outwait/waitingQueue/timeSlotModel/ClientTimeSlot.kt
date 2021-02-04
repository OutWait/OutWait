package elite.kit.outwait.waitingQueue.timeSlotModel

import org.joda.time.Interval

abstract class ClientTimeSlot(
    interval: Interval,
    val slotCode: String,
    val auxiliaryIdentifier: String
) : TimeSlot(interval)
