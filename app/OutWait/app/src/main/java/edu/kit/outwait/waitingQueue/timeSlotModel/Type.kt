package edu.kit.outwait.waitingQueue.timeSlotModel

/**
 * Enumeration of types that a TimeSlot can have.
 * Types can be equivalent to implemented subclasses
 * to differentiate between them e.g. in switch-cases
 *
 */
enum class Type {
    PAUSE,
    FIXED_SLOT,
    SPONTANEOUS_SLOT,
    HEADER
}
