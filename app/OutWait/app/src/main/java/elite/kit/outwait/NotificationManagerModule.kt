package elite.kit.outwait

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import elite.kit.outwait.services.NotifManager


@Module
@InstallIn(ServiceComponent::class)
object NotificationManagerModule {

    // TODO: Injection of NotificationManager wrapper (for easier mocking)

    @notifManager
    @ServiceScoped
    @Provides
    fun provideNotificationManager(@ApplicationContext app: Context)
        = NotifManager(app)
}

