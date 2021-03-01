package edu.kit.outwait.server.management

import java.time.Duration

/**
 * Data class that stores the settings of an institution.
 *
 * @property mode the Mode in which the management operates.
 * @property defaultSlotDuration the preset duration of a slot.
 * @property notificationTime the time before the appointment, after which the client should be
 *     notified.
 * @property delayNotificationTime the minimal delay of a appointment time, after which the client
 *     should be notified.
 * @property prioritizationTime the minimal delay of a spontaneous slot, after which it gets a
 *     higher priority.
 * @constructor Creates the read-only object.
 */
data class ManagementSettings(
    val mode: Mode,
    val defaultSlotDuration: Duration,
    val notificationTime: Duration,
    val delayNotificationTime: Duration,
    val prioritizationTime: Duration,
)
