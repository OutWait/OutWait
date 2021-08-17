package edu.kit.outwait.customDataTypes

import org.joda.time.Duration

/**
 * Bundles the preferences you can configure in the app
 *
 * @property defaultSlotDuration duration of a new client slot if you do not enter a custom one
 * @property notificationTime duration that has to be left before the client slot starts so
 * that the client gets notified
 * @property delayNotificationTime if the slot delays more than this duration,
 * the client gets notified.
 * @property prioritizationTime duration that has to be over before a spontaneous slot
 * is equally prioritized as a fixed slot
 * @property mode currently our app supports two modes. See specification document K1
 */
class Preferences(
    var defaultSlotDuration: Duration,
    val notificationTime: Duration,
    val delayNotificationTime: Duration,
    val prioritizationTime: Duration,
    var mode: Mode
)
