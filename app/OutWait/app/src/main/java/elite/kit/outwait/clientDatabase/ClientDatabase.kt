package elite.kit.outwait.clientDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(version = 1, entities = [ClientInfo::class])
@TypeConverters(Converter::class)
abstract class ClientDatabase : RoomDatabase() {
    abstract fun clientInfoDao(): ClientInfoDao

    companion object {
        fun create(context: Context): ClientDatabase {
            return Room
                .databaseBuilder(context, ClientDatabase::class.java, "client_db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
