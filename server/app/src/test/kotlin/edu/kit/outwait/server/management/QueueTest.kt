package edu.kit.outwait.server.management

import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.slot.*
import io.mockk.*
import java.time.Duration
import java.util.Calendar
import java.util.Date
import kotlin.math.absoluteValue
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.json.JSONObject
import org.junit.jupiter.api.*

class QueueTest {
    val simpleSlotDurationMinutes = 30
    val simplePrioritizationTime = Duration.ofMinutes(180)
    val calendar = Calendar.getInstance()

    // Is used internally by the queue to position delayed slots
    val delayTimeBuffer = Duration.ofSeconds(20)

    private fun createSimpleSlot(code: String) : Slot {
        calendar.add(Calendar.MINUTE, simpleSlotDurationMinutes)
        val nextDate = calendar.getTime()
        return Slot(
            SlotCode(code),
            Priority.NORMAL,
            nextDate,
            Duration.ofMinutes(simpleSlotDurationMinutes.toLong()),
            nextDate
        )
    }
    private fun createTimedSlot(
        code: String,
        constructorTimeEpochMs: Long,
        durationMinutes: Long = simpleSlotDurationMinutes.toLong(),
        fixedSlot: Boolean = false
    ) : Slot {
        return Slot(
            SlotCode(code),
            if (fixedSlot) Priority.FIX_APPOINTMENT else Priority.NORMAL,
            Date(constructorTimeEpochMs),
            Duration.ofMinutes(durationMinutes),
            Date(constructorTimeEpochMs)
        )
    }
    val simpleTestSlots =
        listOf(
            createSimpleSlot("000000000"),
            createSimpleSlot("111111111"),
            createSimpleSlot("222222222"),
            createSimpleSlot("333333333"),
            createSimpleSlot("444444444"),
            createSimpleSlot("555555555")
        )

    init {
        // Reset for next list
        calendar.setTime(
            Date.from(Date().toInstant() + Duration.ofMinutes(-simpleSlotDurationMinutes.toLong()))
        )
    }
    val simpleNowTestSlots =
        listOf(
            createSimpleSlot("000000000"),
            createSimpleSlot("111111111"),
            createSimpleSlot("222222222"),
            createSimpleSlot("333333333"),
            createSimpleSlot("444444444"),
            createSimpleSlot("555555555")
        )

    private fun movedSlot(slot:Slot, offset:Duration):Slot {
        return slot.copy(
            approxTime = Date.from(slot.approxTime.toInstant() + offset),
            constructorTime = Date.from(slot.constructorTime.toInstant() + offset)
        )
    }
    private fun approxMovedSlot(slot:Slot, offset:Duration):Slot {
        return slot.copy(approxTime = Date.from(slot.approxTime.toInstant() + offset))
    }

    private fun runQueueSort(
        inSlots: List<Slot>,
        prioritizationTime: Duration,
        modifier:(queue:Queue, db:DatabaseWrapper)->Unit = { _, _ -> }
    ): List<Slot> {
        val db = mockk<DatabaseWrapper>()
        var outSlots = slot<List<Slot>>()
        every { db.getSlots(QueueId(1)) } returns inSlots
        every { db.saveSlots(slots=capture(outSlots), QueueId(1)) } returns true

        val queue = Queue(QueueId(1), db)
        modifier(queue, db)
        queue.updateQueue(prioritizationTime)
        queue.storeToDB(db)
        return outSlots.captured
    }

    private fun checkSlotOrder(toCheck: List<Slot>, testList: List<Slot>) {
        assertEquals(testList.size, toCheck.size)
        for (i in 0 until toCheck.size) {
            assertEquals(testList[i].slotCode, toCheck[i].slotCode)
        }
    }

    private fun compareSlot(toCheck: Slot, testSlot: Slot) {
        assertEquals(testSlot.slotCode, toCheck.slotCode)
        assertEquals(testSlot.priority, toCheck.priority)
        assertEquals(testSlot.constructorTime, toCheck.constructorTime)

        // expectedDuration approxTime may be off by a few milliseconds due to the time the
        // algorithm runs.
        assertTrue(
            (toCheck.expectedDuration.toMillis() - testSlot.expectedDuration.toMillis())
                .absoluteValue < 1000
        )
        assertTrue(
            (toCheck.approxTime.getTime() - testSlot.approxTime.getTime()).absoluteValue < 1000
        )
    }
    private fun compareSlots(toCheck: List<Slot>, testList: List<Slot>) {
        assertEquals(testList.size, toCheck.size)
        for (i in 0 until toCheck.size) {
            compareSlot(testList[i], toCheck[i])
        }
    }

    // Very basic checks

    /** Checks whether the queue is loaded and stored properly. */
    @Test
    fun simpleQueueLoading() {
        val db = mockk<DatabaseWrapper>()
        var outSlots = slot<List<Slot>>()
        every { db.getSlots(QueueId(1)) } returns simpleTestSlots
        every { db.saveSlots(slots=capture(outSlots), QueueId(1)) } returns true

        val queue = Queue(QueueId(1), db)
        queue.storeToDB(db)
        assertEquals(simpleTestSlots, outSlots.captured)
    }

    /** Checks whether the json construction works. */
    @Test
    fun simpleQueueLoadingToJSON() {
        val inSlots = simpleTestSlots.toMutableList()

        inSlots[2] = inSlots[2].copy(priority = Priority.FIX_APPOINTMENT)

        val db = mockk<DatabaseWrapper>()
        var outSlots = slot<List<Slot>>()
        every { db.getSlots(QueueId(1)) } returns inSlots
        every { db.saveSlots(slots=capture(outSlots), QueueId(1)) } returns true

        val queue = Queue(QueueId(1), db)

        val json = JSONObject()
        queue.storeToJSON(json)

        // Check whether the json data is right, not whether the json library works...

        // Check if all slots are in the slot order array
        val order = json.getJSONArray("slotOrder")
        assertEquals(inSlots.size, order.length())
        for (i in 0 until order.length()) {
            assertEquals(inSlots[i].slotCode.code, order.getString(i))
        }

        // Check if all slots can be found in the other two arrays
        val fixedSlots = json.getJSONArray("fixedSlots")
        val spontaneousSlots = json.getJSONArray("spontaneousSlots")
        assertEquals(inSlots.size, fixedSlots.length() + spontaneousSlots.length())
        for (slot in order) {
            var occurrences = 0
            for (i in 0 until fixedSlots.length()) {
                if (fixedSlots.getJSONObject(i).getString("slotCode") == slot.toString())
                    occurrences++
            }
            for (i in 0 until spontaneousSlots.length()) {
                if (spontaneousSlots.getJSONObject(i).getString("slotCode") == slot.toString())
                    occurrences++
            }
            // Exactly one (checks also that the arrays contain no copies)
            assertEquals(1, occurrences)
        }
    }

    // Tests for the algorithm

    /** Checks whether simple queue sorting in mode 1 works. */
    @Test
    fun simpleQueueSorting() {
        val inSlots =
            listOf(
                simpleTestSlots[3],
                simpleTestSlots[4],
                simpleTestSlots[2],
                simpleTestSlots[5],
                simpleTestSlots[0],
                simpleTestSlots[1],
            )

        val outSlots = runQueueSort(inSlots, simplePrioritizationTime)
        assertNotEquals(outSlots, inSlots)
        checkSlotOrder(outSlots, simpleTestSlots)
    }

    /**
     * Checks whether queue sorting in mode 1 still works, even when the prioritization time is very
     * short.
     */
    @Test
    fun simpleQueueSortingWithShortPrioTime() {
        val inSlots =
            listOf(
                simpleTestSlots[3],
                simpleTestSlots[4],
                simpleTestSlots[2],
                simpleTestSlots[5],
                simpleTestSlots[0],
                simpleTestSlots[1],
            )

        val outSlots = runQueueSort(inSlots, Duration.ofMinutes(5))
        checkSlotOrder(outSlots, simpleTestSlots)
    }

    /** Checks whether the sorting works with an empty queue. */
    @Test
    fun emptyQueue() {
        val inSlots = listOf<Slot>()
        val outSlots = runQueueSort(inSlots, simplePrioritizationTime)
        compareSlots(outSlots, listOf<Slot>())
    }

    /** Checks whether the sporting with a single spontaneous slot works. */
    @Test
    fun singleSpontaneousSlot() {
        val nextDate = Date();
        val singleSlot =
            Slot(
                SlotCode("000000000"),
                Priority.NORMAL,
                nextDate,
                Duration.ofMinutes(simpleSlotDurationMinutes.toLong()),
                nextDate
            )

        val inSlots = listOf(singleSlot)

        val outSlots = runQueueSort(inSlots, simplePrioritizationTime)

        val checkSlots = listOf(singleSlot)

        compareSlots(outSlots, checkSlots)
    }

    /**
     * Checks whether the sporting with a single spontaneous slot that is positioned in the future
     * works.
     */
    @Test
    fun singleSpontaneousLaterSlot() {
        val nextDate = Date.from(Date().toInstant() + Duration.ofMinutes(30));
        val singleSlot =
            Slot(
                SlotCode("000000000"),
                Priority.NORMAL,
                nextDate,
                Duration.ofMinutes(simpleSlotDurationMinutes.toLong()),
                nextDate
            )
        val inSlots = listOf(singleSlot)

        val outSlots = runQueueSort(inSlots, simplePrioritizationTime)

        val checkSlots = listOf(singleSlot.copy(approxTime = Date.from(Date().toInstant())))

        compareSlots(outSlots, checkSlots)
    }

    /** Checks whether the sporting with a single fix slot works. */
    @Test
    fun singleFixStartedSlot() {
        val nextDate = Date();
        val singleSlot =
            Slot(
                SlotCode("000000000"),
                Priority.FIX_APPOINTMENT,
                nextDate,
                Duration.ofMinutes(simpleSlotDurationMinutes.toLong()),
                nextDate
            )
        val inSlots = listOf(singleSlot)

        val outSlots = runQueueSort(inSlots, simplePrioritizationTime)

        val checkSlots = listOf(singleSlot)

        compareSlots(outSlots, checkSlots)
    }

    /**
     * Checks whether the sporting with a single fix slot that is positioned in the future works.
     */
    @Test
    fun singleFixNotStartedSlot() {
        val nextDate = Date.from(Date().toInstant() + Duration.ofMinutes(30));
        val singleSlot =
            Slot(
                SlotCode("000000000"),
                Priority.FIX_APPOINTMENT,
                nextDate,
                Duration.ofMinutes(simpleSlotDurationMinutes.toLong()),
                nextDate
            )
        val inSlots = listOf(singleSlot)

        val outSlots = runQueueSort(inSlots, simplePrioritizationTime)

        val checkSlots = listOf(singleSlot) // The fix slot was not moved!

        compareSlots(outSlots, checkSlots)
    }

    /** Checks whether the sorting with an overdue slot works. */
    @Test
    fun overdueSlots() {
        val inSlots = simpleTestSlots.toMutableList()
        var timeOffset = Duration.ofMinutes(-simpleSlotDurationMinutes.toLong() * 2);
        var timeOffsetWithAlgorithmTime = timeOffset;
        inSlots[0] = movedSlot(inSlots[0], timeOffset)

        val nowMillis = Date().getTime()
        val outSlots = runQueueSort(inSlots, simplePrioritizationTime)

        // Add the time, the algorithm took
        val algorithmTime = Duration.ofMillis(Date().getTime() - nowMillis)
        timeOffsetWithAlgorithmTime += algorithmTime

        // The first's duration will be slightly different (depending on the test conditions)
        val firstSlotMoved = movedSlot(simpleTestSlots[0], timeOffset)
        val checkSlots =
            listOf(
                firstSlotMoved.copy(
                    expectedDuration =
                        firstSlotMoved.expectedDuration + algorithmTime + delayTimeBuffer
                ),
                approxMovedSlot(simpleTestSlots[1], timeOffsetWithAlgorithmTime),
                approxMovedSlot(simpleTestSlots[2], timeOffsetWithAlgorithmTime),
                approxMovedSlot(simpleTestSlots[3], timeOffsetWithAlgorithmTime),
                approxMovedSlot(simpleTestSlots[4], timeOffsetWithAlgorithmTime),
                approxMovedSlot(simpleTestSlots[5], timeOffsetWithAlgorithmTime)
            )

        compareSlots(outSlots, checkSlots)
    }

    /** Checks whether sorting with multiple not jet started slots work. */
    @Test
    fun notJetStartedSlots() {
        val nowMillis = Date().getTime()
        val outSlots = runQueueSort(simpleTestSlots, simplePrioritizationTime)

        // Calculate the time, the algorithm took
        val algorithmTime = Duration.ofMillis(Date().getTime() - nowMillis)
        var timeOffset = Duration.ofMinutes(-simpleSlotDurationMinutes.toLong()) + algorithmTime;

        // The slots will be moved the above delay into the past
        val checkSlots =
            listOf(
                approxMovedSlot(simpleTestSlots[0], timeOffset),
                approxMovedSlot(simpleTestSlots[1], timeOffset),
                approxMovedSlot(simpleTestSlots[2], timeOffset),
                approxMovedSlot(simpleTestSlots[3], timeOffset),
                approxMovedSlot(simpleTestSlots[4], timeOffset),
                approxMovedSlot(simpleTestSlots[5], timeOffset)
            )

        compareSlots(outSlots, checkSlots)
    }

    /** Checks whether sorting with multiple slots of different type works. */
    @Test
    fun simpleMode2Slots() {
        val inSlots = simpleNowTestSlots.toMutableList()

        inSlots[1] =
            movedSlot(inSlots[1], Duration.ofMinutes(20)).copy(priority = Priority.FIX_APPOINTMENT)
        inSlots[3] =
            movedSlot(inSlots[3], Duration.ofMinutes(60)).copy(priority = Priority.FIX_APPOINTMENT)

        val outSlots = runQueueSort(inSlots, simplePrioritizationTime)

        val checkSlots =
            listOf(
                inSlots[0],
                inSlots[1],
                approxMovedSlot(inSlots[2], Duration.ofMinutes(20)),
                approxMovedSlot(inSlots[4], Duration.ofMinutes(20) - inSlots[3].expectedDuration),
                inSlots[3],
                approxMovedSlot(inSlots[5], Duration.ofMinutes(60) - inSlots[3].expectedDuration)
            )

        compareSlots(outSlots, checkSlots)
    }

    /** Checks whether sorting with multiple not jet started slots of different type works. */
    @Test
    fun notStartedMode2Slots() {
        val inSlots = simpleTestSlots.toMutableList()

        inSlots[1] =
            movedSlot(inSlots[1], Duration.ofMinutes(20)).copy(priority = Priority.FIX_APPOINTMENT)
        inSlots[3] =
            movedSlot(inSlots[3], Duration.ofMinutes(60)).copy(priority = Priority.FIX_APPOINTMENT)

        val nowMillis = Date().getTime()
        val outSlots = runQueueSort(inSlots, simplePrioritizationTime)

        // Calculate the time, the algorithm took
        val algorithmTime = Duration.ofMillis(Date().getTime() - nowMillis)
        var timeOffset = Duration.ofMinutes(-simpleSlotDurationMinutes.toLong());

        val checkSlots =
            listOf(
                approxMovedSlot(inSlots[0], timeOffset + algorithmTime),
                approxMovedSlot(inSlots[2], timeOffset + timeOffset + algorithmTime),
                inSlots[1],
                approxMovedSlot(
                    inSlots[4],
                    timeOffset + Duration.ofMinutes(20) - inSlots[3].expectedDuration
                ),
                approxMovedSlot(
                    inSlots[5],
                    timeOffset + Duration.ofMinutes(20) - inSlots[3].expectedDuration
                ),
                inSlots[3]
            )

        compareSlots(outSlots, checkSlots)
    }

    /** Checks whether sorting with slots of different length works. */
    @Test
    fun mixWithDifferentLengths() {
        val inSlots = simpleNowTestSlots.toMutableList()

        inSlots[1] = inSlots[1].copy(expectedDuration = Duration.ofMinutes(45))
        inSlots[2] = inSlots[2].copy(expectedDuration = Duration.ofMinutes(5))
        inSlots[3] = inSlots[3].copy(expectedDuration = Duration.ofMinutes(300))
        inSlots[4] = inSlots[4].copy(expectedDuration = Duration.ofMinutes(60))
        inSlots[5] = inSlots[5].copy(expectedDuration = Duration.ofMinutes(10))

        val outSlots = runQueueSort(inSlots, simplePrioritizationTime)

        val checkSlots =
            listOf(
                inSlots[0],
                inSlots[1],
                approxMovedSlot(
                    inSlots[2],
                    Duration.ofMinutes(45 - simpleSlotDurationMinutes.toLong())
                ),
                approxMovedSlot(
                    inSlots[3],
                    Duration.ofMinutes(50 - simpleSlotDurationMinutes.toLong() * 2)
                ),
                approxMovedSlot(
                    inSlots[4],
                    Duration.ofMinutes(350 - simpleSlotDurationMinutes.toLong() * 3)
                ),
                approxMovedSlot(
                    inSlots[5],
                    Duration.ofMinutes(410 - simpleSlotDurationMinutes.toLong() * 4)
                )
            )

        compareSlots(outSlots, checkSlots)
    }

    /**
     * Checks whether a scenario where a fix slot is prioritized over another spontaneous slot
     * works.
     */
    @Test
    fun mode2Priority() {
        val inSlots = simpleNowTestSlots.toMutableList()

        inSlots[3] =
            movedSlot(inSlots[3], Duration.ofMinutes(-20)).copy(priority = Priority.FIX_APPOINTMENT)

        val outSlots = runQueueSort(inSlots, simplePrioritizationTime)

        val checkSlots =
            listOf(
                inSlots[0],
                inSlots[1],
                inSlots[3],
                approxMovedSlot(
                    inSlots[2],
                    Duration.ofMinutes(simpleSlotDurationMinutes.toLong() * 2 - 20)
                ),
                approxMovedSlot(
                    inSlots[4],
                    Duration.ofMinutes(simpleSlotDurationMinutes.toLong() - 20)
                ),
                approxMovedSlot(
                    inSlots[5],
                    Duration.ofMinutes(simpleSlotDurationMinutes.toLong() - 20)
                )
            )

        compareSlots(outSlots, checkSlots)
    }

    /**
     * Checks whether a scenario where a spontaneous slot is prioritized over another fix slot
     * works.
     */
    @Test
    fun prioritizationOfSpontaneousSlot() {
        val inSlots = simpleNowTestSlots.toMutableList()

        inSlots[2] =
            movedSlot(inSlots[2], Duration.ofMinutes(-simpleSlotDurationMinutes.toLong()))
                .copy(priority = Priority.FIX_APPOINTMENT)
        inSlots[3] =
            movedSlot(inSlots[3], Duration.ofMinutes(-simpleSlotDurationMinutes.toLong()))
                .copy(priority = Priority.FIX_APPOINTMENT)
        inSlots[4] =
            movedSlot(inSlots[4], Duration.ofMinutes(-simpleSlotDurationMinutes.toLong()))
                .copy(priority = Priority.FIX_APPOINTMENT)
        inSlots[5] =
            movedSlot(inSlots[5], Duration.ofMinutes(-simpleSlotDurationMinutes.toLong()))
                .copy(priority = Priority.FIX_APPOINTMENT)

        val outSlots =
            runQueueSort(inSlots, Duration.ofMinutes(simpleSlotDurationMinutes.toLong() * 3))

        // Slot 1 has to be inserted between 4 and 5 not after 5! This tests ">" vs ">=" bugs.
        val checkSlots =
            listOf(
                inSlots[0],
                inSlots[2],
                inSlots[3],
                inSlots[4],
                approxMovedSlot(
                    inSlots[1],
                    Duration.ofMinutes(simpleSlotDurationMinutes.toLong() * 3)
                ).copy(priority = Priority.URGENT),
                approxMovedSlot(inSlots[5], Duration.ofMinutes(simpleSlotDurationMinutes.toLong()))
            )

        compareSlots(outSlots, checkSlots)
    }

    /**
     * Checks whether remaining spontaneous slots (ones after the last fix slot) are appendend
     * properly.
     */
    @Test
    fun multipleRemainingSpontaneousSlots() {
        val inSlots = simpleNowTestSlots.toMutableList()

        inSlots[1] = inSlots[1].copy(priority = Priority.FIX_APPOINTMENT)
        inSlots[2] = movedSlot(inSlots[2], Duration.ofMinutes(20))
        inSlots[3] = movedSlot(inSlots[3], Duration.ofMinutes(20))
        inSlots[4] = movedSlot(inSlots[4], Duration.ofMinutes(60))
        inSlots[5] = movedSlot(inSlots[5], Duration.ofMinutes(120))

        val outSlots = runQueueSort(inSlots, simplePrioritizationTime)

        val checkSlots =
            listOf(
                inSlots[0],
                inSlots[1],
                approxMovedSlot(inSlots[2], Duration.ofMinutes(-20)),
                approxMovedSlot(inSlots[3], Duration.ofMinutes(-20)),
                approxMovedSlot(inSlots[4], Duration.ofMinutes(-60)),
                approxMovedSlot(inSlots[5], Duration.ofMinutes(-120))
            )

        compareSlots(outSlots, checkSlots)
    }

    /**
     * Checks whether remaining fix slots (ones after the last spontaneous slot) are appendend
     * properly.
     */
    @Test
    fun multipleRemainingFixSlots() {
        val inSlots = simpleNowTestSlots.toMutableList()

        inSlots[2] =
            movedSlot(inSlots[2], Duration.ofMinutes(20)).copy(priority = Priority.FIX_APPOINTMENT)
        inSlots[3] =
            movedSlot(inSlots[3], Duration.ofMinutes(20)).copy(priority = Priority.FIX_APPOINTMENT)
        inSlots[4] =
            movedSlot(inSlots[4], Duration.ofMinutes(60)).copy(priority = Priority.FIX_APPOINTMENT)
        inSlots[5] =
            movedSlot(inSlots[5], Duration.ofMinutes(120)).copy(priority = Priority.FIX_APPOINTMENT)

        val outSlots = runQueueSort(inSlots, simplePrioritizationTime)

        val checkSlots =
            listOf(inSlots[0], inSlots[1], inSlots[2], inSlots[3], inSlots[4], inSlots[5])

        compareSlots(outSlots, checkSlots)
    }

    /** Tests sorting for a complex example. */
    @Test
    fun complexExample() {
        // Reset the calendar for this test
        calendar.setTime(
            Date.from(Date().toInstant() + Duration.ofMinutes(-simpleSlotDurationMinutes.toLong()))
        )

        val now = Date().getTime()
        val inSlots =
            listOf(
                createTimedSlot("xxxxxxxx0", now + Duration.ofMinutes(10).toMillis(), 20, true),
                createTimedSlot("xxxxxxxx1", now + Duration.ofMinutes(1).toMillis()),
                createTimedSlot("xxxxxxxx2", now + Duration.ofMinutes(110).toMillis(), 30, true),
                createTimedSlot("xxxxxxxx3", now + Duration.ofMinutes(2).toMillis()),
                createTimedSlot("xxxxxxxx4", now + Duration.ofMinutes(170).toMillis(), 30, true),
                createTimedSlot("xxxxxxxx5", now + Duration.ofMinutes(90).toMillis()),
                createTimedSlot("xxxxxxxx6", now + Duration.ofMinutes(210).toMillis(), 30, true),
                createTimedSlot("xxxxxxxx7", now + Duration.ofMinutes(190).toMillis(), 20),
                createTimedSlot("xxxxxxxx8", now + Duration.ofMinutes(230).toMillis(), 20),
                createTimedSlot("xxxxxxxx9", now + Duration.ofMinutes(250).toMillis(), 20, true),
                createTimedSlot("xxxxxxx10", now + Duration.ofMinutes(340).toMillis(), 20, true),
                createTimedSlot("xxxxxxx11", now + Duration.ofMinutes(350).toMillis(), 20, true),
                createTimedSlot("xxxxxxx12", now + Duration.ofMinutes(390).toMillis(), 20, true),
                createTimedSlot("xxxxxxx13", now + Duration.ofMinutes(460).toMillis(), 20, true),
                createTimedSlot("xxxxxxx14", now + Duration.ofMinutes(290).toMillis()),
                createTimedSlot("xxxxxxx15", now + Duration.ofMinutes(400).toMillis(), 40),
                createTimedSlot("xxxxxxx16", now + Duration.ofMinutes(470).toMillis(), 10),
                createTimedSlot("xxxxxxx17", now + Duration.ofMinutes(480).toMillis(), 20)
            )

        val outSlots = runQueueSort(inSlots, Duration.ofMinutes(30))

        val checkSlots =
            listOf(
                inSlots[0],
                approxMovedSlot(inSlots[1], Duration.ofMinutes(29)),
                approxMovedSlot(inSlots[3], Duration.ofMinutes(58))
                    .copy(priority = Priority.URGENT),
                inSlots[2],
                approxMovedSlot(inSlots[5], Duration.ofMinutes(50))
                    .copy(priority = Priority.URGENT),
                inSlots[4],
                inSlots[6],
                approxMovedSlot(inSlots[7], Duration.ofMinutes(50))
                    .copy(priority = Priority.URGENT),
                approxMovedSlot(inSlots[8], Duration.ofMinutes(30))
                    .copy(priority = Priority.URGENT),
                approxMovedSlot(inSlots[9], Duration.ofMinutes(30)),
                approxMovedSlot(inSlots[14], Duration.ofMinutes(10)),
                inSlots[10],
                approxMovedSlot(inSlots[11], Duration.ofMinutes(10)),
                inSlots[12],
                approxMovedSlot(inSlots[15], Duration.ofMinutes(10)),
                approxMovedSlot(inSlots[16], Duration.ofMinutes(-20)),
                inSlots[13],
                inSlots[17],
            )

        compareSlots(outSlots, checkSlots)
    }

    /** Checks whether slots that were constructed to early, are removed properly. */
    @Test
    fun removeSlotsWithInvalidTime() {
        val inSlots = simpleNowTestSlots.toMutableList()

        inSlots[1] = movedSlot(inSlots[1], Duration.ofHours(-25)) // This slot is kept
        inSlots[2] = movedSlot(inSlots[2], Duration.ofHours(-50)) // Mind current delta from now
        inSlots[4] = movedSlot(inSlots[4], Duration.ofHours(48)) // This slot is kept

        val nowMillis = Date().getTime()

        var deletedSlots = mutableListOf<SlotCode>()
        val modifier = { _: Queue, db: DatabaseWrapper ->
            every { db.deleteSlot(capture(deletedSlots)) } returns true

            Unit
        }
        val outSlots = runQueueSort(inSlots, simplePrioritizationTime, modifier)

        // Calculate the time, the algorithm took
        val algorithmTime = Duration.ofMillis(Date().getTime() - nowMillis)

        val checkSlots =
            listOf(
                inSlots[1]
                    .copy(
                        expectedDuration =
                            Duration.ofMinutes(-simpleSlotDurationMinutes.toLong() + 25 * 60) +
                                delayTimeBuffer + algorithmTime
                    ),
                approxMovedSlot(inSlots[0], algorithmTime),
                approxMovedSlot(
                    inSlots[3],
                    Duration.ofMinutes(-simpleSlotDurationMinutes.toLong() * 2) + algorithmTime
                ),
                approxMovedSlot(
                    inSlots[5],
                    Duration.ofMinutes(-simpleSlotDurationMinutes.toLong() * 3) + algorithmTime
                ),
                approxMovedSlot(
                    inSlots[4],
                    Duration.ofMinutes(-simpleSlotDurationMinutes.toLong() - 48 * 60) +
                        algorithmTime
                )
            )

        compareSlots(outSlots, checkSlots)

        assertEquals(listOf(inSlots[2].slotCode), deletedSlots)
    }

    // Tests for manipulating the queue

    /** Checks whether adding of a slot works. */
    @Test
    fun addSlot() {
        val inSlots = simpleNowTestSlots.toMutableList()

        inSlots.removeAt(5)

        val modifier = { queue: Queue, _: DatabaseWrapper ->
            queue.updateQueue(simplePrioritizationTime)
            queue.addSlot(simpleNowTestSlots[5]) // Re-add the previously removed slot

            Unit
        }
        val outSlots = runQueueSort(inSlots, simplePrioritizationTime, modifier)

        val checkSlots =
            listOf(
                inSlots[0],
                inSlots[1],
                inSlots[2],
                inSlots[3],
                inSlots[4],
                simpleNowTestSlots[5]
            )

        compareSlots(outSlots, checkSlots)
    }

    /** Checks whether deletion of a slot works. */
    @Test
    fun deleteSlot() {
        val inSlots = simpleNowTestSlots.toMutableList()

        var deletedSlots = mutableListOf<SlotCode>()
        val modifier = { queue: Queue, db: DatabaseWrapper ->
            every { db.deleteSlot(capture(deletedSlots)) } returns true

            queue.updateQueue(simplePrioritizationTime)
            queue.deleteSlot(inSlots[2].slotCode)

            Unit
        }
        val outSlots = runQueueSort(inSlots, simplePrioritizationTime, modifier)

        val checkSlots =
            listOf(
                inSlots[0],
                inSlots[1],
                approxMovedSlot(
                    inSlots[3],
                    Duration.ofMinutes(-simpleSlotDurationMinutes.toLong())
                ),
                approxMovedSlot(
                    inSlots[4],
                    Duration.ofMinutes(-simpleSlotDurationMinutes.toLong())
                ),
                approxMovedSlot(inSlots[5], Duration.ofMinutes(-simpleSlotDurationMinutes.toLong()))
            )

        compareSlots(outSlots, checkSlots)
        assertEquals(listOf(inSlots[2].slotCode), deletedSlots)
    }

    /** Checks whether deletion of the first slot works. */
    @Test
    fun endSlot() {
        val inSlots = simpleNowTestSlots.toMutableList()

        val nowMillis = Date().getTime()

        var endedSlots = mutableListOf<SlotCode>()
        val modifier = { queue: Queue, db: DatabaseWrapper ->
            every { db.endSlot(capture(endedSlots)) } returns true

            queue.updateQueue(simplePrioritizationTime)
            queue.endCurrentSlot()

            Unit
        }
        val outSlots = runQueueSort(inSlots, simplePrioritizationTime, modifier)

        // Calculate the time, the algorithm took
        val algorithmTime = Duration.ofMillis(Date().getTime() - nowMillis)

        val checkSlots =
            listOf(
                approxMovedSlot(
                    inSlots[1],
                    Duration.ofMinutes(-simpleSlotDurationMinutes.toLong()) + algorithmTime
                ),
                approxMovedSlot(
                    inSlots[2],
                    Duration.ofMinutes(-simpleSlotDurationMinutes.toLong()) + algorithmTime
                ),
                approxMovedSlot(
                    inSlots[3],
                    Duration.ofMinutes(-simpleSlotDurationMinutes.toLong()) + algorithmTime
                ),
                approxMovedSlot(
                    inSlots[4],
                    Duration.ofMinutes(-simpleSlotDurationMinutes.toLong()) + algorithmTime
                ),
                approxMovedSlot(
                    inSlots[5],
                    Duration.ofMinutes(-simpleSlotDurationMinutes.toLong()) + algorithmTime
                )
            )

        compareSlots(outSlots, checkSlots)
        assertEquals(listOf(inSlots[0].slotCode), endedSlots)
    }

    /** Checks whether moving of slots works. */
    @Test
    fun moveSlot() {
        val inSlots = simpleNowTestSlots.toMutableList()

        val modifier = { queue: Queue, _: DatabaseWrapper ->
            queue.updateQueue(simplePrioritizationTime)
            queue.moveSlotAfterAnother(inSlots[3].slotCode, inSlots[1].slotCode)

            Unit
        }
        val outSlots = runQueueSort(inSlots, simplePrioritizationTime, modifier)

        val checkSlots =
            listOf(
                inSlots[0],
                inSlots[1],
                approxMovedSlot(inSlots[3], Duration.ofMinutes(-simpleSlotDurationMinutes.toLong()))
                    .copy(
                        constructorTime =
                            Date.from(inSlots[1].constructorTime.toInstant() + Duration.ofMillis(1))
                    ),
                approxMovedSlot(inSlots[2], Duration.ofMinutes(simpleSlotDurationMinutes.toLong())),
                inSlots[4],
                inSlots[5]
            )

        compareSlots(outSlots, checkSlots)
    }

    /** Checks whether updating slot times works. */
    @Test
    fun changeSlotTime() {
        val inSlots = simpleNowTestSlots.toMutableList()

        inSlots[3] = inSlots[3].copy(priority = Priority.FIX_APPOINTMENT)

        val modifier = { queue: Queue, _: DatabaseWrapper ->
            queue.updateQueue(simplePrioritizationTime)
            queue.changeAppointmentTime(
                inSlots[3].slotCode,
                Date.from(inSlots[3].constructorTime.toInstant() + Duration.ofMinutes(10))
            )

            Unit
        }
        val outSlots = runQueueSort(inSlots, simplePrioritizationTime, modifier)

        val checkSlots =
            listOf(
                inSlots[0],
                inSlots[1],
                inSlots[2],
                movedSlot(inSlots[3], Duration.ofMinutes(10)),
                approxMovedSlot(inSlots[4], Duration.ofMinutes(10)),
                approxMovedSlot(inSlots[5], Duration.ofMinutes(10))
            )

        compareSlots(outSlots, checkSlots)
    }

    /** Checks whether updating slot lengths works. */
    @Test
    fun changeSlotLength() {
        val inSlots = simpleNowTestSlots.toMutableList()

        val modifier = { queue: Queue, _: DatabaseWrapper ->
            queue.updateQueue(simplePrioritizationTime)
            queue.updateSlotLength(
                inSlots[3].slotCode,
                inSlots[3].expectedDuration + Duration.ofMinutes(10)
            )

            Unit
        }
        val outSlots = runQueueSort(inSlots, simplePrioritizationTime, modifier)

        val checkSlots =
            listOf(
                inSlots[0],
                inSlots[1],
                inSlots[2],
                inSlots[3]
                    .copy(expectedDuration = inSlots[3].expectedDuration + Duration.ofMinutes(10)),
                approxMovedSlot(inSlots[4], Duration.ofMinutes(10)),
                approxMovedSlot(inSlots[5], Duration.ofMinutes(10))
            )

        compareSlots(outSlots, checkSlots)
    }
}
