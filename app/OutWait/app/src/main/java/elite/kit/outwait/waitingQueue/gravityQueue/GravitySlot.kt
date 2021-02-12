package elite.kit.outwait.waitingQueue.gravityQueue

import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval


abstract class GravitySlot (val slotCode: String, val duration: Duration) {
    abstract fun interval(scheduledStart : DateTime) : Interval
}
