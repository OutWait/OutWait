package elite.kit.outwait.customDataTypes

import org.joda.time.Duration

class Preferences (
    val defaultSlotDuration : Duration,
    val notificationTime : Duration,
    val delayNotificationTime : Duration,
    val prioritizationTime : Duration,
    val mode: Mode
)
