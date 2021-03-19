package elite.kit.outwait.instituteDatabase.rooms

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DBLoginDataDao {

    @Insert
    suspend fun insert(loginData: DBLoginData)

    @Query("DELETE FROM login_data")
    suspend fun clearTable()

    @Query("Select * From login_data")
    suspend fun getAllLoginData(): List<DBLoginData>
}
