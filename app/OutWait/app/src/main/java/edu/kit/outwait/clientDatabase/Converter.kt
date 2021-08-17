package edu.kit.outwait.clientDatabase

import androidx.room.TypeConverter
import org.joda.time.DateTime
import org.joda.time.Duration

/**
 * Converter with room annotations so that room can automatically convert
 * Joda time classes to Long-Timestamps that it can store in the database
 * and vice versa.
 *
 */
class Converter {

    /**
     * Transforms unix timestamp to Joda DateTime
     *
     * @param stamp date as unix timestamp
     * @return date as Joda DateTime
     */
    @TypeConverter
    fun fromTimeStamp(stamp: Long): DateTime = DateTime(stamp)

    /**
     * Transforms Joda DateTime to unix timestamp
     *
     * @param dateTime date as Joda DateTime
     * @return date as unix timestamp
     */
    @TypeConverter
    fun toTimeStamp(dateTime: DateTime): Long = dateTime.millis

    /**
     * Transforms duration in milliseconds to a Joda Duration
     *
     * @param stamp duration in milliseconds
     * @return duration as Joda Duration
     */
    @TypeConverter
    fun fromTimeStampDuration(stamp: Long): Duration = Duration.millis(stamp)

    /**
     * Transforms Joda Duration to a duration in milliseconds.
     *
     * @param duration duration as Joda Duration
     * @return duration in milliseconds
     */
    @TypeConverter
    fun toTimeStampDuration(duration: Duration): Long = duration.millis
}
