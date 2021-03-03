package elite.kit.outwait.utils

import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter


object TransformationOutput {

    private val formatter: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")


    fun appointmentToString(appointmentTime: DateTime):String{
        return formatter.print(appointmentTime)
    }

    fun intervalToString(interval: Interval):String{
        return  formatter.print(interval.toDurationMillis())
    }

    fun durationToString(duration: Duration): String{
        //return formatter.print(duration.millis)
        return "" + duration.standardHours + ":" + duration.standardMinutes
    }
}
