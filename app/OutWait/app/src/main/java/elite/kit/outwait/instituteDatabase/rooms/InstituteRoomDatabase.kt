package elite.kit.outwait.instituteDatabase.rooms

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//BEI ÄNDERUNGEN VERSION ANPASSEN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

@Database(version = 1, entities = [DBAuxiliaryIdentifier::class])
abstract class InstituteRoomDatabase : RoomDatabase() {

    abstract fun getDBAuxiliaryIdentifierDao(): DBAuxiliaryIdentifierDao

    companion object {
        fun create(context: Context): InstituteRoomDatabase {
            return Room
                .databaseBuilder(context, InstituteRoomDatabase::class.java, "institute_db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }

}
