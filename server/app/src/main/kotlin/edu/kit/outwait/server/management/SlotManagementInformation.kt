package edu.kit.outwait.server.management

import java.time.Duration

/**
 * Data class that stores information about an institution, that is send to the client.file
 *
 * @property details general information about an institution
 * @property notificationTime the time before the appointment, after which the client should be
 *     notified.
 * @property delayNotificationTime the minimal delay of a appointment time, after which the client
 *     should be notified.
 * @constructor Creates the read-only object.
 */
data class SlotManagementInformation(
    val details: ManagementDetails,
    val notificationTime: Duration,
    val delayNotificationTime: Duration,
)
