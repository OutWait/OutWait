package edu.kit.outwait.server.slot

/**
 * Enumeration of possible priorities of a slot.
 *
 * The priority also defines whether the slot is a fix slot or a spontaneous slot.
 */
enum class Priority {
    /** Normal spontaneous slot with low priority. */
    NORMAL,
    /** Fix slot with elevated priority. */
    FIX_APPOINTMENT,
    /** Spontaneous slot that reached its maximal waiting time and gets highest priority. */
    URGENT,
}
