package elite.kit.outwait

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import elite.kit.outwait.clientDatabase.ClientDatabase
import elite.kit.outwait.clientDatabase.ClientInfoDao
import elite.kit.outwait.remoteDataSource.HandlerFactory
import elite.kit.outwait.remoteDataSource.SocketIOHandlerFactory
import elite.kit.outwait.services.ServiceHandler
import elite.kit.outwait.services.TimerService
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object MainModule {

    @Provides
    @Singleton
    fun provideClientDatabase(@ApplicationContext context: Context): ClientDatabase
        = ClientDatabase.create(context)

    @Provides
    @Singleton
    fun provideClientInfoDao(clientDatabase: ClientDatabase): ClientInfoDao
        = clientDatabase.clientInfoDao()

    @Provides
    @Singleton
    fun provideClientHandler(dao: ClientInfoDao)
        = SocketIOHandlerFactory().buildClientHandler(dao)

    @Provides
    @Singleton
    fun provideManagementHandler()
        = SocketIOHandlerFactory().buildManagementHandler()

    @Provides
    @Singleton
    fun provideServiceHandler(@ApplicationContext context: Context)
        = ServiceHandler(context)
}
