package edu.kit.outwait.waitingQueue.gravityQueue

import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval

/**
 * This class is models a time slot with gravity characteristics.
 * For basic information about the gravity concept, see chapter 10.1
 * in the design document.
 *
 * @property duration how much time is scheduled for the slot. Note that
 * a duration has no absolute beginning and ending point in time.
 */
abstract class GravitySlot (val duration: Duration) {
    /**
     * This method models the gravity behaviour of the slot. Given the first
     * possible point in time when the slot could begin, in which time interval
     * will it take place? (more information: Chapter 10.1 in the design document)
     *
     * @param earliestPossibleStart the first possible point in time when the slot could begin
     * @return interval when the slot takes place if [earliestPossibleStart] is the
     * earliest possible beginning point in time
     */
    abstract fun interval(earliestPossibleStart : DateTime) : Interval
}
