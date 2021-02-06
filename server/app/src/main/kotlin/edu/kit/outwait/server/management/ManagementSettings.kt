package edu.kit.outwait.server.management

import java.time.Duration

data class ManagementSettings(
    val mode: Mode,
    val defaultSlotDuration: Duration,
    val notificationTime: Duration,
    val delayNotificationTime: Duration,
    val prioritizationTime: Duration,
)
