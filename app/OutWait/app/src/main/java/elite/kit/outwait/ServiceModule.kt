package elite.kit.outwait

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import elite.kit.outwait.services.*

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(@ApplicationContext app: Context) = PendingIntent.getActivity(
        app,
        0,
        Intent(app, MainActivity::class.java), 0 //TODO: FLAG_UPDATE_CURRENT -> hilft das mit issue#31?
    )

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
}
