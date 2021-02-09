package elite.kit.outwait.clientDatabase

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import org.joda.time.DateTime
import org.joda.time.Duration

class Converter {
    @TypeConverter
    fun fromTimeStamp(stamp: Long): DateTime? {
        if (stamp != 0L){
            return DateTime(stamp)
        } else{
            return null
        }
    }

    @TypeConverter
    fun toTimeStamp(dateTime : DateTime?): Long{
        if (dateTime != null) {
            return dateTime.millis
        }
        else{
            return 0
        }
    }

   @TypeConverter
    fun fromTimeStampDuration(stamp : Long) : Duration?{
       if (stamp != 0L){
           return Duration.millis(stamp)
       } else {
           return null
       }
    }

    @TypeConverter
    fun toTimeStampDuration(dateTime : Duration?) : Long{
        if (dateTime != null) {
            return dateTime.millis
        } else {
            return 0
        }
    }
}
