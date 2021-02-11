package elite.kit.outwait.utils

import org.joda.time.DateTime
import org.joda.time.Interval

object TransformationOutput {

    const val SEPERATOR=":"

    fun appointmentToString(appointmenttime: DateTime):String{
        return appointmenttime.hourOfDay.toString()+SEPERATOR+appointmenttime.minuteOfHour.toString()
    }

    fun intervalToString(interval: Interval):String{
        val hours= interval.toDuration().standardHours
        val restTime=interval.toDuration().minus(hours)
        val minutes=restTime.standardMinutes
        return hours.toString()+SEPERATOR+minutes.toString()
    }
}
