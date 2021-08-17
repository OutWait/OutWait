package edu.kit.outwait.waitingQueue.gravityQueue

import edu.kit.outwait.customDataTypes.FixedSlot
import edu.kit.outwait.customDataTypes.ReceivedList
import edu.kit.outwait.customDataTypes.SpontaneousSlot
import edu.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
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
import kotlin.random.Random

/**
 * Tests the receivedListToTimeSlotList() method of the GravityQueueConverter
 * class, which applies the gravity algorithm as well as matches auxiliary
 * Identifiers to their slots
 *
 */
class GravityQueueConverterTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `input of empty queue produces empty queue output`() {
        val receivedList = ReceivedList(
            DateTime.parse("2021-03-15T11:20:00.000+01:00"),
            listOf(),
            listOf(),
            listOf()
        )
        val auxIdentifiers = HashMap<String, String>()

        val converter = GravityQueueConverter()
        val timeSlotList = converter.receivedListToTimeSlotList(receivedList, auxIdentifiers)

        Assert.assertTrue(timeSlotList.isEmpty())
    }

    @Test
    fun `number of slots is consistent`(){
        //choose the numbers you want
        val nrOfSlots = 12
        val slotDurationInMinutes = 30L

        val spontaneousSlots = mutableListOf<SpontaneousSlot>()
        val order = mutableListOf<String>()
        for (i in 1..nrOfSlots) {
            order.add(i.toString())
            spontaneousSlots.add(
                SpontaneousSlot(
                    Duration.standardMinutes(slotDurationInMinutes),
                    i.toString()
                )
            )
        }
        val receivedList = ReceivedList(
            DateTime.now(),
            order,
            spontaneousSlots,
            listOf()
        )
        val auxIdentifiers = HashMap<String, String>()

        val converter = GravityQueueConverter()
        val timeSlotList = converter.receivedListToTimeSlotList(receivedList, auxIdentifiers)

        Assert.assertEquals(nrOfSlots, timeSlotList.size)
    }

    @Test
    fun `aux Identifiers get matched correctly`(){
        //choose the numbers you want
        val nrOfSlots = 12
        val slotDurationInMinutes = 30L

        val spontaneousSlots = mutableListOf<SpontaneousSlot>()
        val order = mutableListOf<String>()
        val auxIdentifiers = hashMapOf<String, String>()
        for (i in 1..nrOfSlots) {
            order.add(i.toString())
            spontaneousSlots.add(
                SpontaneousSlot(
                    Duration.standardMinutes(slotDurationInMinutes),
                    i.toString()
                )
            )
            /*
            For every Slot we set the slotCode as auxiliary identifier so that
            we can compare easily if they are matched correctly to the TimeSlots
            */
            auxIdentifiers[i.toString()] = i.toString()
        }
        val receivedList = ReceivedList(
            DateTime.now(),
            order,
            spontaneousSlots,
            listOf()
        )

        val converter = GravityQueueConverter()
        val timeSlotList = converter.receivedListToTimeSlotList(receivedList, auxIdentifiers)

        for (slot in timeSlotList){
            val clientSlot = slot as ClientTimeSlot
            Assert.assertEquals(clientSlot.slotCode, clientSlot.auxiliaryIdentifier)
        }
    }

    /*
    IMPORTANT: receivedListToTimeSlotList() requests the current system time.
    If the System time changes during the test more tan [slotDurationInMinutes],
    there can be side effects. This is almost impossible, so we don´t mock the
    system time, but for the completeness of the documentation,
    in theory it is possible
    */
    @Test
    fun `summation of times is correct`(){
        //choose the numbers you want
        val nrOfSlots = 12
        val slotDurationInMinutes = 30L

        val spontaneousSlots = mutableListOf<SpontaneousSlot>()
        val order = mutableListOf<String>()
        val auxIdentifiers = hashMapOf<String, String>()
        for (i in 1..nrOfSlots) {
            order.add(i.toString())
            spontaneousSlots.add(
                SpontaneousSlot(
                    Duration.standardMinutes(slotDurationInMinutes),
                    i.toString()
                )
            )
        }

        val now = DateTime.now()
        val receivedList = ReceivedList(
            now,
            order,
            spontaneousSlots,
            listOf()
        )


        val converter = GravityQueueConverter()
        val timeSlotList = converter.receivedListToTimeSlotList(receivedList, auxIdentifiers)

        val lastSlotEnd = timeSlotList.last().interval.end
        val lastSlotRequiredEnd = now + Duration.standardMinutes(nrOfSlots * slotDurationInMinutes)
        Assert.assertEquals(lastSlotEnd, lastSlotRequiredEnd)
    }

    /*
    This Method passes a list of three slots to the GravityQueueConverters
    receivedListToTimeSlotList() method. The List contains a fixed slot which does
    not directly follow the precedent spontaneous slot, so the method
    receivedListToTimeSlotList() has to insert a pause.

    Firstly, we assert that the slots are in the right order and that the pause
    is inserted in the right place.
    Secondly, we check if the pause and the fixed slot have the correct starting
    and ending time
     */
    @Test
    fun `fixed slot not before appointment time and insert pause in free time`(){

        val auxIdentifiers = hashMapOf<String, String>()
        val now = DateTime.now()

        val spontaneous1 = SpontaneousSlot(
            Duration.standardMinutes(20),
            "spontaneous1"
        )
        val fixed1 = FixedSlot(
            Duration.standardMinutes(30),
            "fixed1",
            now.plus(Duration.standardHours(1))
        )
        val spontaneous2 = SpontaneousSlot(
            Duration.standardMinutes(50),
            "spontaneous2"
        )
        val order = mutableListOf<String>("spontaneous1", "fixed1", "spontaneous2")
        val spontaneousSlots = mutableListOf(spontaneous2, spontaneous1)
        val fixedSlots = mutableListOf(fixed1)
        val receivedList = ReceivedList(
            now,
            order,
            spontaneousSlots,
            fixedSlots
        )

        val converter = GravityQueueConverter()
        val timeSlotList = converter.receivedListToTimeSlotList(receivedList, auxIdentifiers)

        Assert.assertTrue(timeSlotList[0] is SpontaneousTimeSlot)
        Assert.assertTrue(timeSlotList[1] is Pause)
        Assert.assertTrue(timeSlotList[2] is FixedTimeSlot)
        Assert.assertTrue(timeSlotList[3] is SpontaneousTimeSlot)

        val pauseInterval = Interval(
            now + Duration.standardMinutes(20),
            now + Duration.standardHours(1)
        )
        Assert.assertEquals(timeSlotList[1].interval, pauseInterval)

        val fixedSlotInterval = Interval(
            now + Duration.standardHours(1),
            now + Duration.standardMinutes(90)
        )
        Assert.assertEquals(timeSlotList[2].interval, fixedSlotInterval)
    }
}
