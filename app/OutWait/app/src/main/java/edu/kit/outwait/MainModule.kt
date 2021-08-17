package edu.kit.outwait

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.kit.outwait.clientDatabase.ClientDatabase
import edu.kit.outwait.clientDatabase.ClientInfoDao
import edu.kit.outwait.instituteDatabase.facade.InstituteDBFacade
import edu.kit.outwait.instituteDatabase.facade.InstituteRoomDBFacade
import edu.kit.outwait.instituteDatabase.rooms.DBAuxiliaryIdentifierDao
import edu.kit.outwait.instituteDatabase.rooms.DBLoginDataDao
import edu.kit.outwait.instituteDatabase.rooms.InstituteRoomDatabase
import edu.kit.outwait.remoteDataSource.SocketIOHandlerFactory
import edu.kit.outwait.services.ServiceHandler
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

    @Provides
    @Singleton
    fun provideInstituteRoomDatabase(@ApplicationContext context: Context): InstituteRoomDatabase
        = InstituteRoomDatabase.create(context)

    @Provides
    @Singleton
    fun provideDBAuxiliaryIdentifierDao(db: InstituteRoomDatabase)
        = db.getDBAuxiliaryIdentifierDao()

    @Provides
    @Singleton
    fun provideDBLoginDataDao(db: InstituteRoomDatabase)
        = db.getDBLoginDataDao()

    @Provides
    @Singleton
    fun provideInstituteDBFacade(auxDao: DBAuxiliaryIdentifierDao, loginDao: DBLoginDataDao): InstituteDBFacade
        = InstituteRoomDBFacade(auxDao, loginDao)
}
