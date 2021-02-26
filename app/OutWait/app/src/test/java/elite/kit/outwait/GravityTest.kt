package elite.kit.outwait

import elite.kit.outwait.customDataTypes.FixedSlot
import elite.kit.outwait.customDataTypes.ReceivedList
import elite.kit.outwait.customDataTypes.SpontaneousSlot
import elite.kit.outwait.waitingQueue.gravityQueue.GravityQueueConverter
import elite.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.FixedTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.Pause
import elite.kit.outwait.waitingQueue.timeSlotModel.SpontaneousTimeSlot
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval
import org.junit.Assert
import org.junit.Test
import kotlin.random.Random

/**
 * Tests the receivedListToTimeSlotList() method of the GravityQueueConverter
 * class, which applies the gravity algorithm as well as matches auxiliary
 * Identifiers to their slots
 *
 */
class GravityTest {
    @Test
    fun `input of empty queue produces empty queue output`() {
        val receivedList = ReceivedList(
            DateTime.now(),
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
        val spontaneousSlots = mutableListOf<SpontaneousSlot>()
        val order = mutableListOf<String>()
        val nrOfSlots = Random.nextInt(10)
        for (i in 1..nrOfSlots) {
            order.add(i.toString())
            spontaneousSlots.add(
                SpontaneousSlot(
                    Duration.standardMinutes(1 + Random.nextInt(30).toLong()),
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
        val spontaneousSlots = mutableListOf<SpontaneousSlot>()
        val order = mutableListOf<String>()
        val auxIdentifiers = hashMapOf<String, String>()
        val nrOfSlots = 10
        for (i in 1..nrOfSlots) {
            order.add(i.toString())
            spontaneousSlots.add(
                SpontaneousSlot(
                    Duration.standardMinutes(1 + Random.nextInt(30).toLong()),
                    i.toString()
                )
            )
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
            Assert.assertEquals(clientSlot.slotCode.toString(), clientSlot.auxiliaryIdentifier)
        }
    }

    @Test
    fun `summation of times is correct`(){
        val spontaneousSlots = mutableListOf<SpontaneousSlot>()
        val order = mutableListOf<String>()
        val auxIdentifiers = hashMapOf<String, String>()
        val nrOfSlots = 10
        val slotDuration = 30L
        for (i in 1..nrOfSlots) {
            order.add(i.toString())
            spontaneousSlots.add(
                SpontaneousSlot(
                    Duration.standardMinutes(slotDuration),
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
        val lastSlotRequiredEnd = now + Duration.standardMinutes(nrOfSlots * slotDuration)
        Assert.assertEquals(lastSlotEnd, lastSlotRequiredEnd)
    }

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
            DateTime.now().plus(Duration.standardHours(1))
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
