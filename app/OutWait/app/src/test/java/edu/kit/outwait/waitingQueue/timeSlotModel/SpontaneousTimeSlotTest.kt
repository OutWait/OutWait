package edu.kit.outwait.waitingQueue.timeSlotModel

import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test

class SpontaneousTimeSlotTest {
    @Test
    fun `constructor sets all values properly`(){
        val duration = Duration.standardMinutes(120)
        val slotCode = "JS4DH3F4U"
        val aux = "Herr Meier OP"
        val begin = DateTime.parse("2021-03-15T11:20:00.000+01:00")
        val interval = Interval(begin, begin + duration)

        val slot = SpontaneousTimeSlot(interval, slotCode, aux)

        Assert.assertEquals(slot.slotCode, slotCode)
        Assert.assertEquals(slot.auxiliaryIdentifier, aux)
        Assert.assertEquals(slot.interval, interval)
    }

    @Test
    fun `type is correct`(){
        val duration = Duration.standardMinutes(120)
        val slotCode = "JS4DH3F4U"
        val aux = "Herr Meier OP"
        val begin = DateTime.parse("2021-03-15T11:20:00.000+01:00")
        val interval = Interval(begin, begin + duration)

        val slot = SpontaneousTimeSlot(interval, slotCode, aux)

        assertEquals(slot.getType(), Type.SPONTANEOUS_SLOT)
    }
}
