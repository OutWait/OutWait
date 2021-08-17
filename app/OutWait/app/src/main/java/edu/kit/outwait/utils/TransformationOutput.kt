package edu.kit.outwait.utils

import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.PeriodFormatterBuilder


object TransformationOutput {

    private val formatter: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
    private val periodFormatter = PeriodFormatterBuilder()
        .minimumPrintedDigits(2)
        .printZeroAlways()
        .appendHours()
        .appendLiteral(":")
        .printZeroAlways()
        .appendMinutes()
        .toFormatter()

    fun appointmentToString(appointmentTime: DateTime):String{
        return formatter.print(appointmentTime)
    }

    fun intervalToString(interval: Interval):String{
        return periodFormatter.print(interval.toPeriod())
    }

    fun durationToString(duration: Duration): String{
        return periodFormatter.print(duration.toPeriod())
    }
}
