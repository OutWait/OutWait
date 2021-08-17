package edu.kit.outwait.instituteDatabase.rooms

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * This abstract class is required by the room framework so that it
 * can build the database.
 * The database "institute_db" has a table that stores all the auxiliary identifiers
 * entered by the receptionist and another table for login data.
 *
 */
@Database(
    version = 2,
    entities = [DBAuxiliaryIdentifier::class, DBLoginData::class]
)
abstract class InstituteRoomDatabase : RoomDatabase() {

    /**
     * returns object that can access the aux_identifiers Table
     *
     * @return object that can access the aux_identifiers Table
     */
    abstract fun getDBAuxiliaryIdentifierDao(): DBAuxiliaryIdentifierDao

    /**
     * returns object that can access the login_data Table
     *
     * @return object that can access the login_data Table
     */
    abstract fun getDBLoginDataDao(): DBLoginDataDao

    companion object {
        /**
         * Tells Android to create the database.
         *
         * @param context the App context is required here
         * @return the database object
         */
        fun create(context: Context): InstituteRoomDatabase {
            return Room
                .databaseBuilder(context, InstituteRoomDatabase::class.java, "institute_db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }

}
