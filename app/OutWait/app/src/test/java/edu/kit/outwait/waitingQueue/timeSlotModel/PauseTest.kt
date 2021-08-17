package edu.kit.outwait.waitingQueue.timeSlotModel

import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test

class PauseTest {
    @Test
    fun `constructor sets interval properly`(){
        val duration = Duration.standardMinutes(120)
        val begin = DateTime.parse("2021-03-15T11:20:00.000+01:00")
        val interval = Interval(begin, begin + duration)

        val pause = Pause(interval)

        Assert.assertEquals(pause.interval, interval)
    }

    @Test
    fun `type is correct`(){
        val duration = Duration.standardMinutes(120)
        val begin = DateTime.parse("2021-03-15T11:20:00.000+01:00")
        val interval = Interval(begin, begin + duration)

        val pause = Pause(interval)

        assertEquals(pause.getType(), Type.PAUSE)
    }
}
