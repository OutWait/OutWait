package edu.kit.outwait.server.management

import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.slot.Priority
import edu.kit.outwait.server.slot.Slot
import edu.kit.outwait.server.slot.SlotCode
import java.time.Duration
import java.util.Date

class Queue(
    val managementId: ManagementId,
    val queueId: QueueId,
    databaseWrapper: DatabaseWrapper
) {
    private var slots = mutableListOf<Slot>()

    init {
        slots = databaseWrapper.getSlots(queueId).toMutableList()
    }
    fun updateQueue(prioritizationTime: Duration) {
        if (slots.isEmpty()) return // Don't run the algorithm, if no slots exist

        val newQueue = mutableListOf<Slot>() // The new queue

        val spontaneousSlots =
            slots.filter { it.priority != Priority.FIX_APPOINTMENT }.toMutableList()
        val fixSlots = slots.filter { it.priority == Priority.FIX_APPOINTMENT }.toMutableList()

        // If the first slot has already started, it can not be moved
        if (slots[0].approxTime.before(Date())) {
            newQueue.add(slots[0])
            spontaneousSlots.remove(slots[0])
            fixSlots.remove(slots[0])
        }

        // Sort the array by their creation time
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

            // Check if the next spontaneous slot fits in between the line and the next fix slot and
            // if the next spontaneous slot is not prioritized
            if ((line + nextSpontaneous.expectedDuration)
                .isAfter(nextFix.constructorTime.toInstant()) &&
                (nextSpontaneous.constructorTime.toInstant() + prioritizationTime).isAfter(line)
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
    fun calculateNextDelayChange(): Date {
        // TODO
        return Date()
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
        val slot = slots.find { it.slotCode == slotToMove }
        if (slot != null) {
            slots.remove(slot)
            val targetIndex = slots.indexOfFirst { it.slotCode == otherSlot }
            slots.add(targetIndex + 1, slot)
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
