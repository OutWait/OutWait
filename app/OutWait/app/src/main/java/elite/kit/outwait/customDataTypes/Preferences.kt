package elite.kit.outwait.customDataTypes

import org.joda.time.Duration

class Preferences(
    var defaultSlotDuration: Duration,
    val notificationTime: Duration,
    val delayNotificationTime: Duration,
    val prioritizationTime: Duration,
    var mode: Mode
)
