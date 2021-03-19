package elite.kit.outwait.instituteDatabase.rooms

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "login_data")
data class DBLoginData (
    @PrimaryKey
    val username: String,
    val password: String
)
