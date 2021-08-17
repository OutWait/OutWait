package edu.kit.outwait

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import edu.kit.outwait.services.NotifManager
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NotificationManagerModule {

    /*
    @notifManager
    @ServiceScoped
    @Provides

     */
    @notifManager
    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext app: Context)
        = NotifManager(app)
}

