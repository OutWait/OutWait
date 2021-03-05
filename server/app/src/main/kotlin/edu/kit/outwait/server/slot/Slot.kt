package edu.kit.outwait.server.slot

import java.time.Duration
import java.util.Date

/**
 * Data class that stores all data about a slot.
 *
 * @property slotCode the unique code of the slot.
 * @property priority the slot (also specifies its type).
 * @property approxTime the calculated target time of the slot.
 * @property expectedDuration the expected duration/length of the slot.
 * @property constructorTime For spontaneous slots, this is the time when the slot has been created
 *     and for fix slots, this is the expected appointment time.
 * @constructor Creates the read-only object.
 */
data class Slot(
    val slotCode: SlotCode,
    val priority: Priority,
    val approxTime: Date,
    val expectedDuration: Duration,
    val constructorTime: Date,
) {
    /**
     * Helper function to detect whether a slot is a fix slot or a spontaneous slot.
     *
     * @return true if the slot is a fix slot, otherwise false.
     */
    fun isFixedSlot(): Boolean = priority == Priority.FIX_APPOINTMENT
}
