package edu.kit.outwait.server.management

/**
 * Enumeration of possible modes, in that a institution can operate.
 *
 * @property ONE the first mode, that only allows spontaneous slots, which are served in a
 *     fifo-queue.
 * @property TWO the second mode, that also supports fixed slots.
 */
enum class Mode {
    ONE,
    TWO,
}
