package edu.kit.outwait

import javax.inject.Qualifier

/**
 * Qualifier, specifying the injection of the first notification builder of type NotificationCompat.Builder
 *
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class channel_1NotificationBuilder

/**
 * Qualifier, specifying the injection of the second notification builder of type NotificationCompat.Builder
 *
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class channel_2NotificationBuilder

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class notifManager
