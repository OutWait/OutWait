package edu.kit.outwait.waitingQueue.timeSlotModel

import org.joda.time.Interval

/**
 * This class represents a time slot with its currently predicted time interval
 *
 * @property interval the time interval that is currently predicted for this slot
 */
abstract class TimeSlot(val interval: Interval) {
    /**
     * returns what type of time slot it is (e.g. Pause or Scheduled Client time slot)
     *
     * @return type of time slot (e.g. Pause or Scheduled Client time slot)
     */
    abstract fun getType(): Type
}
