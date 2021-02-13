package edu.kit.outwait.server.management

import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.slot.Priority
import edu.kit.outwait.server.slot.Slot
import edu.kit.outwait.server.slot.SlotCode
import java.time.Duration
import java.util.Date

class Queue(val queueId: QueueId, databaseWrapper: DatabaseWrapper) {
    private var slots = mutableListOf<Slot>()
    private var delayChangeTime: Date? = null

    init {
        slots = databaseWrapper.getSlots(queueId).toMutableList()
    }
    fun updateQueue(prioritizationTime: Duration) {
        delayChangeTime = null
        if (slots.isEmpty()) {
            return // Don't run the algorithm, if no slots exist
        }

        val newQueue = mutableListOf<Slot>() // The new queue

        val spontaneousSlots =
            slots.filter { it.priority != Priority.FIX_APPOINTMENT }.toMutableList()
        val fixSlots = slots.filter { it.priority == Priority.FIX_APPOINTMENT }.toMutableList()

        // If the first slot has already started, it can not be moved
        slots.sortWith(compareBy { it.approxTime })// Sort the queue, to find the current slot
        if (slots[0].approxTime.before(Date())) {
            newQueue.add(slots[0])
            spontaneousSlots.remove(slots[0])
            fixSlots.remove(slots[0])
        }

        // Sort the slots by their creation time
        spontaneousSlots.sortWith(compareBy { it.constructorTime })
        fixSlots.sortWith(compareBy { it.constructorTime })

        // Start the sweep line after the running slot or at the current time
        var line =
            if (newQueue.isNotEmpty())
                newQueue[0].approxTime.toInstant() + newQueue[0].expectedDuration
            else
                Date().toInstant()

        // Construct the new queue using a sweep line-like algorithm
        while (spontaneousSlots.isNotEmpty() && fixSlots.isNotEmpty()) {
            val nextSpontaneous = spontaneousSlots[0]
            val nextFix = fixSlots[0]

            val timeToNextFixSlot =
                Duration.ofMillis(
                    Math.max(
                        0,
                        nextFix.constructorTime.toInstant().toEpochMilli() - line.toEpochMilli()
                    )
                )
            val remainingPrioritizationBuffer =
                prioritizationTime -
                    Duration.ofMillis(
                        line.toEpochMilli() -
                            nextSpontaneous.constructorTime.toInstant().toEpochMilli()
                    )
            // Check if the next spontaneous slot fits in between the line and the next fix slot and
            // if the next spontaneous slot is not prioritized
            if ((timeToNextFixSlot - nextSpontaneous.expectedDuration).isNegative() &&
                !remainingPrioritizationBuffer.isNegative()
            ) {
                // Choose the fix slot
                val chosenSlot =
                    nextFix.copy(
                        approxTime =
                            if (line.isAfter(nextFix.constructorTime.toInstant()))
                                Date.from(line)
                            else
                                nextFix.constructorTime
                    )
                newQueue.add(chosenSlot)
                fixSlots.removeAt(0)

                // Store delay time
                if (delayChangeTime == null && newQueue.isNotEmpty()) {
                    // timeToNextFixSlot seems to be the only variable which indicates when a
                    // "non-trivial" change happens
                    delayChangeTime =
                        Date.from(
                            newQueue[0].approxTime.toInstant() + newQueue[0].expectedDuration +
                                timeToNextFixSlot
                        )
                }

                line = chosenSlot.approxTime.toInstant() + chosenSlot.expectedDuration
            } else {
                val chosenSlot = nextSpontaneous.copy(approxTime = Date.from(line))
                newQueue.add(chosenSlot)
                spontaneousSlots.removeAt(0)

                line += chosenSlot.expectedDuration
            }
        }

        // Add remaining slots
        if (spontaneousSlots.isNotEmpty()) {
            newQueue.addAll(spontaneousSlots)
        } else if (fixSlots.isNotEmpty()) {
            newQueue.addAll(fixSlots)
        }

        // Finalize the queue
        slots = newQueue
    }
    fun calculateNextDelayChange(): Date? {
        return delayChangeTime
    }
    fun storeToDB(databaseWrapper: DatabaseWrapper) {
        databaseWrapper.saveSlots(slots, queueId)
    }
    fun addSpontaneousSlot(slot: Slot) {
        slots.add(slot);
    }
    fun addFixedSlot(slot: Slot) {
        slots.add(slot);
    }
    fun deleteSlot(slotCode: SlotCode) {
        slots.removeIf({ it.slotCode == slotCode })
    }
    fun endCurrentSlot() {
        if (slots.isNotEmpty()) {
            slots.removeAt(0)
        }
    }
    fun moveSlotAfterAnother(slotToMove: SlotCode, otherSlot: SlotCode) {
        // TODO this implementation will not work (updateQueue will revert the change)
        // TODO maybe we should use a more stable approach to store the slots in the queue (maybe
        //  multiple lists and spontaneous slots are sorted by index instead of creation time)
        val slot = slots.find { it.slotCode == slotToMove }
        val targetSlot = slots.find { it.slotCode == otherSlot }

        if (slot != null && targetSlot != null) {
            slots.remove(slot)
            val newDate = Date.from(targetSlot.constructorTime.toInstant() + Duration.ofSeconds(1))
            val conflictingSlot = slots.find { it.constructorTime == newDate }

            slots.add(slot.copy(constructorTime=newDate))

            if (conflictingSlot != null) {
                // Move the conflicting slots recursively
                moveSlotAfterAnother(conflictingSlot.slotCode, slot.slotCode)
            }
        }
    }

    /** Replaces a slot in the list with a updated slot */
    private fun replaceSlot(oldSlot:Slot, newSlot:Slot) {
        val index = slots.indexOf(oldSlot)
        slots.remove(oldSlot)
        slots.add(index, newSlot)
    }

    fun changeAppointmentTime(slotCode: SlotCode, newTime: Date) {
        var oldSlot = slots.find { it.slotCode == slotCode }
        if (oldSlot != null) {
            replaceSlot(oldSlot, oldSlot.copy(constructorTime=newTime))
        }
    }
    fun updateSlotLength(slotCode: SlotCode, newLength: Duration) {
        var oldSlot = slots.find { it.slotCode == slotCode }
        if (oldSlot != null) {
            replaceSlot(oldSlot, oldSlot.copy(expectedDuration=newLength))
        }
    }
}
