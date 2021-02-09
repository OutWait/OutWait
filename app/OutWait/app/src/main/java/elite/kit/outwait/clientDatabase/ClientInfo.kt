package elite.kit.outwait.clientDatabase

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime
import org.joda.time.Duration

@Entity(tableName = "client_table")
data class ClientInfo (
    @PrimaryKey val slotCode : String,
    val institutionName: String,
    val approximatedTime: DateTime,
    val originalAppointmentTime: DateTime,
    val notificationTime: Duration,
    val delayNotificationTime: Duration
    )
