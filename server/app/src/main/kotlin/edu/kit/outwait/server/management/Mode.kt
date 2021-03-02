package edu.kit.outwait.server.management

/** Enumeration of possible modes, in that a institution can operate. */
enum class Mode {
    /** First mode, that only allows spontaneous slots, which are served in a fifo-queue. */
    ONE,
    /** Second mode, that also supports fixed slots. */
    TWO,
}
