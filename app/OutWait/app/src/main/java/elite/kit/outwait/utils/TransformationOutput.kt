package elite.kit.outwait.utils

import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter


object TransformationOutput {

    const val SEPERATOR=":"

    fun appointmentToString(appointmentTime: DateTime):String{
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
        return formatter.print(appointmentTime)
    }

    fun intervalToString(interval: Interval):String{
        val hours= interval.toDuration().standardHours
        val restTime=interval.toDuration().minus(hours * 60 * 60 * 1000)
        val minutes=restTime.standardMinutes
        return hours.toString()+SEPERATOR+minutes.toString()
    }
}
