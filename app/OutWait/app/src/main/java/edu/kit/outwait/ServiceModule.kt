package edu.kit.outwait

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import edu.kit.outwait.services.*

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    /**
     * Dependency Injection, providing a pending intent for the main activity
     *
     * @param app the application context
     */
    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(@ApplicationContext app: Context) = PendingIntent.getActivity(
        app, 0, Intent(app, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)


    /**
     * Dependency Injection, providing the NotificationBuilder for the permanent push notifications
     * posted on the first notification channel
     *
     * @param app the application context
     * @param pendingIntent the pending intent for the main activity, used to navigate back into the app
     * on touch of the respective notification
     */
    @channel_1NotificationBuilder
    @ServiceScoped
    @Provides
    fun providePermNotificationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent) = NotificationCompat.Builder(app, PERM_CHANNEL_ID)
        .setContentTitle(PERM_CHANNEL_DEFAULT_TITLE)
        .setContentText(NOTIFICATION_CHANNEL_DEFAULT_TEXT)
        .setSmallIcon(R.drawable.ic_timer)
        .setContentIntent(pendingIntent)
        // needed for devices with API Level 25 or lower
        .setPriority(NotificationCompat.PRIORITY_LOW)

    /**
     * Dependency Injection, providing the NotificationBuilder for the non-permanent push notifications
     * posted on the second notification channel
     *
     * @param app the application context
     * @param pendingIntent the pending intent for the main activity, used to navigate back into the app
     * on touch of the respective notification
     */
    @channel_2NotificationBuilder
    @ServiceScoped
    @Provides
    fun provideSecondNotificationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app, SECOND_CHANNEL_ID)
        .setContentTitle(SECOND_CHANNEL_DEFAULT_TITLE)
        .setContentText(NOTIFICATION_CHANNEL_DEFAULT_TEXT)
        .setSmallIcon(R.drawable.ic_timer)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        // needed for devices with API Level 25 or lower
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    /* //TODO: moved in NotificationManagerModule
    @notifManager
    @ServiceScoped
    @Provides
    fun provideNotificationManager(@ApplicationContext app: Context)
        = NotifManager(app)

     */
}
