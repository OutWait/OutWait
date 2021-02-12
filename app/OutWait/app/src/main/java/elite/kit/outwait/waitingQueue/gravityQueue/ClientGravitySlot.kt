package elite.kit.outwait.waitingQueue.gravityQueue

import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import org.joda.time.Duration

abstract class ClientGravitySlot (slotCode: String, duration: Duration, val auxiliaryIdentifier: String = "Hans") : GravitySlot(slotCode, duration){
    abstract fun toClientTimeSlot(predecessor: TimeSlot): TimeSlot
}
