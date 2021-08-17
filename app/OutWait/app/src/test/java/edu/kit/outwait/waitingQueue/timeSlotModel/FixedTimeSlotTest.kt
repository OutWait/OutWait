package edu.kit.outwait.waitingQueue.timeSlotModel

import edu.kit.outwait.waitingQueue.gravityQueue.FixedGravitySlot
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test

class FixedTimeSlotTest{

    @Test
    fun `constructor sets all values properly`(){
        val duration = Duration.standardMinutes(120)
        val slotCode = "JS4DH3F4U"
        val aux = "Herr Meier OP"
        val appointmentTime = DateTime.parse("2021-03-15T11:20:00.000+01:00")
        val interval = Interval(appointmentTime, appointmentTime + duration)

        val slot = FixedTimeSlot(interval, slotCode, aux, appointmentTime)

        Assert.assertEquals(slot.slotCode, slotCode)
        Assert.assertEquals(slot.auxiliaryIdentifier, aux)
        Assert.assertEquals(slot.interval, interval)
        Assert.assertEquals(slot.appointmentTime, appointmentTime)
    }

    @Test
    fun `type is correct`(){
        val duration = Duration.standardMinutes(120)
        val slotCode = "JS4DH3F4U"
        val aux = "Herr Meier OP"
        val appointmentTime = DateTime.parse("2021-03-15T11:20:00.000+01:00")
        val interval = Interval(appointmentTime, appointmentTime + duration)

        val slot = FixedTimeSlot(interval, slotCode, aux, appointmentTime)

        assertEquals(slot.getType(), Type.FIXED_SLOT)
    }
}
