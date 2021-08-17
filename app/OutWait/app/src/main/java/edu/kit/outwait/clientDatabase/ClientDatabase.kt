package edu.kit.outwait.clientDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * This abstract class is required by the room framework so that
 * can build the database.
 * In the only table "client_table" can be stored the information
 * about the clients active slots.
 *
 */
@Database(version = 1, entities = [ClientInfo::class])
@TypeConverters(Converter::class)
abstract class ClientDatabase : RoomDatabase() {
    /**
     * returns object that can access the clientInfo Table
     *
     * @return object that can access the clientInfo Table
     */
    abstract fun clientInfoDao(): ClientInfoDao

    companion object {
        /**
         * Tells Android to create the database.
         *
         * @param context the App context is required here
         * @return the database object
         */
        fun create(context: Context): ClientDatabase {
            return Room
                .databaseBuilder(context, ClientDatabase::class.java, "client_db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
