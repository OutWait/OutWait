package elite.kit.outwait.utils

import android.util.Log
import org.joda.time.DateTime
import org.joda.time.DateTimeFieldType
import org.joda.time.Duration
import org.joda.time.Interval
import java.util.*

object TransformationInput {
    const val START_TIME_DURATION=0L
    const val MILLISECONDS_TO_MINUTE=60000

    @JvmStatic
    fun formatDateTime(hour: Int, minute: Int) : DateTime {
        return  DateTime(DateTime.now().year,
            DateTime.now().monthOfYear,DateTime.now().dayOfMonth, hour,
            minute)
    }

    @JvmStatic
    fun formatInterval(duration:Long) : Interval {
        var start =DateTime(START_TIME_DURATION)
        var end = start.plus(duration)
        return Interval(start,end)
    }



}
