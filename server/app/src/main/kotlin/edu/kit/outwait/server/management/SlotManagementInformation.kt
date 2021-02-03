package edu.kit.outwait.server.management

import java.time.Duration

class SlotManagementInformation(
    val details: ManagementDetails,
    val notificationTime: Duration,
    val delayNotificationTime: Duration,
)
