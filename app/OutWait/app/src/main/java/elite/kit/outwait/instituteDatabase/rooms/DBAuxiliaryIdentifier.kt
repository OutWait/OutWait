package elite.kit.outwait.instituteDatabase.rooms

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime
import org.joda.time.Duration

@Entity(tableName = "aux_identifiers")
data class DBAuxiliaryIdentifier (
        @PrimaryKey val slotCode : String,
        val auxiliaryText: String,
)
