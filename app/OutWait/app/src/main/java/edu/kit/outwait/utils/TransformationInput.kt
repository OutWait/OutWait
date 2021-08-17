package edu.kit.outwait.utils

import org.joda.time.DateTime
import org.joda.time.Interval

object TransformationInput {
    private const val START_TIME_DURATION=0L

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
