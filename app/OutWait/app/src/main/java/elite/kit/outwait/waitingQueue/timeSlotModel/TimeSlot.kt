package elite.kit.outwait.waitingQueue.timeSlotModel

import org.joda.time.Interval

/**
 * This class represents a time slot with starting point and ending point in time
 *
 * @property interval includes starting- and ending point of the slot
 */
abstract class TimeSlot(val interval: Interval) {
    /**
     * returns what type of time slot it is (e.g. Pause or Scheduled Client time slot)
     *
     * @return type of time slot (e.g. Pause or Scheduled Client time slot)
     */
    abstract fun getType() : Type
}
