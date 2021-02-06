package edu.kit.outwait.server.slot

import java.time.Duration
import java.util.Date

data class Slot(
    val slotCode: SlotCode,
    val priority: Priority,
    val approxTime: Date,
    val expectedDuration: Duration,
    val constructorTime: Date,
)
