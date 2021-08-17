package edu.kit.outwait.instituteDatabase.rooms

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DBLoginDataDao {
    /**
     * Inserts a new row of login data into the database
     *
     * @param loginData the new login data
     */
    @Insert
    suspend fun insert(loginData: DBLoginData)

    /**
     * deletes all login data from the database
     *
     */
    @Query("DELETE FROM login_data")
    suspend fun clearTable()

    /**
     * returns list of all login data saved in the database
     *
     * @return list of all login data saved in the database
     */
    @Query("Select * From login_data")
    suspend fun getAllLoginData(): List<DBLoginData>
}
