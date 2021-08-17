package edu.kit.outwait.waitingQueue.gravityQueue

import edu.kit.outwait.waitingQueue.timeSlotModel.FixedTimeSlot
import edu.kit.outwait.waitingQueue.timeSlotModel.Pause
import edu.kit.outwait.waitingQueue.timeSlotModel.SpontaneousTimeSlot
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval
import org.junit.After
import org.junit.Assert
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test

class SpontaneousGravitySlotTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `constructor sets all values properly`(){
        val slotCode = "JS4DH3F4U"
        val aux = "Herr Meier Schmerzen"
        val duration = Duration.standardMinutes(130)

        val slot = SpontaneousGravitySlot(slotCode, duration, aux)

        Assert.assertEquals(slot.slotCode, slotCode)
        Assert.assertEquals(slot.auxiliaryIdentifier, aux)
        Assert.assertEquals(slot.duration, duration)
    }

    @Test
    fun `interval is calculated correctly`(){
        val duration = Duration.standardMinutes(20)
        val possibleBegin = DateTime.parse("2021-03-15T11:40:00.000+01:00")

        val expectedInterval = Interval(possibleBegin, possibleBegin + duration)

        val slot = SpontaneousGravitySlot(
            "not relevant",
            duration,
            "not relevant"
        )

        val interval = slot.interval(possibleBegin)

        assertEquals(interval, expectedInterval)
    }

    @Test
    fun `all data can be successfully transformed to a time slot`(){
        val slotCode = "JS4DH3F4U"
        val aux = "Herr Meier OP"
        val duration = Duration.standardMinutes(20)
        val possibleBegin = DateTime.parse("2021-03-15T11:00:00.000+01:00")
        val dummyPredecessor = Pause(Interval(possibleBegin, possibleBegin))

        val expectedInterval = Interval(possibleBegin, possibleBegin + duration)

        val slot = SpontaneousGravitySlot(
            slotCode,
            duration,
            aux
        )

        val timeSlot = slot.toClientTimeSlot(dummyPredecessor) as SpontaneousTimeSlot

        assertEquals(timeSlot.interval, expectedInterval)
        assertEquals(timeSlot.auxiliaryIdentifier, aux)
        assertEquals(timeSlot.slotCode, slotCode)
    }
}
