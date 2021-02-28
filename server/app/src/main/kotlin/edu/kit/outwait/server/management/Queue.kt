package edu.kit.outwait.server.management

import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.core.Logger
import edu.kit.outwait.server.slot.Slot
import edu.kit.outwait.server.slot.SlotCode
import java.time.Duration
import java.util.Date
import org.json.JSONObject

class Queue(val queueId: QueueId, databaseWrapper: DatabaseWrapper) {
    private var slots = mutableListOf<Slot>()
    private var deletedSlots = mutableListOf<SlotCode>()
    private var endedSlots = mutableListOf<SlotCode>()
    private var delayChangeTime: Date? = null
    private val LOG_ID = "QUEUE"

    init {
        Logger.debug(LOG_ID, "Loading new queue with id " + queueId)
        slots = databaseWrapper.getSlots(queueId)!!.toMutableList() // queueId must exist
        Logger.debug(LOG_ID, "Queue loaded")
    }
    fun updateQueue(prioritizationTime: Duration) {
        Logger.debug(LOG_ID, "Updating queue " + queueId)
        delayChangeTime = null
        if (slots.isEmpty()) {
            Logger.debug(LOG_ID, "Queue is empty (no update required)")
            return // Don't run the algorithm, if no slots exist
        }

        val newQueue = mutableListOf<Slot>() // The new queue

        val spontaneousSlots = slots.filter { !it.isFixedSlot() }.toMutableList()
        val fixSlots = slots.filter { it.isFixedSlot() }.toMutableList()

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
        Logger.debug(LOG_ID, "Spontaneous slots of queue: " + spontaneousSlots)
        Logger.debug(LOG_ID, "Fixed slots of queue: " + fixSlots)

        // Start the sweep line after the running slot or at the current time
        var line =
            if (newQueue.isNotEmpty())
                newQueue[0].approxTime.toInstant() + newQueue[0].expectedDuration
            else
                Date().toInstant()

        // Construct the new queue using a sweep line-like algorithm
        Logger.debug(LOG_ID, "Running queue update algorithm...")
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
        Logger.debug(LOG_ID, "Queue update algorithm finished. New queue: " + newQueue)
        Logger.debug(LOG_ID, "Next delay change time: " + delayChangeTime)

        // Finalize the queue
        slots = newQueue
    }
    fun calculateNextDelayChange(): Date? {
        return delayChangeTime
    }
    fun storeToDB(databaseWrapper: DatabaseWrapper) {
        Logger.debug(LOG_ID, "Storing queue " + queueId + " into the DB")
        databaseWrapper.saveSlots(slots, queueId)
        deletedSlots.forEach { databaseWrapper.deleteSlot(it) }
        endedSlots.forEach { databaseWrapper.endSlot(it) }
        deletedSlots.clear()
        endedSlots.clear()
    }
    fun storeToJSON(json: JSONObject) {
        Logger.debug(LOG_ID, "Constructing queue " + queueId + " json...")

        var currentSlotStartedTime = Date()
        if (slots.isNotEmpty() && slots[0].approxTime.before(currentSlotStartedTime)) {
            // First slot is currently running
            currentSlotStartedTime = slots[0].approxTime
            Logger.debug(LOG_ID, "First slot is currently running")
        }

        json.put("currentSlotStartedTime", currentSlotStartedTime.getTime())
        json.put("slotOrder", slots.map { it.slotCode.code })
        json.put(
            "spontaneousSlots",
            slots.filter { !it.isFixedSlot() }
                .map {
                    val tmp = JSONObject()
                    tmp.put("slotCode", it.slotCode.code)
                    tmp.put("duration", it.expectedDuration.toMillis())
                    tmp
                }
        )
        json.put(
            "fixedSlots",
            slots.filter { it.isFixedSlot() }
                .map {
                    val tmp = JSONObject()
                    tmp.put("slotCode", it.slotCode.code)
                    tmp.put("appointmentTime", it.approxTime.getTime())
                    tmp.put("duration", it.expectedDuration.toMillis())
                    tmp
                }
        )
        Logger.debug(LOG_ID, "Queue json constructed: " + json)
    }

    fun addSpontaneousSlot(slot: Slot) {
        Logger.debug(LOG_ID, "Adding spontaneous slot " + slot + " to " + queueId)
        slots.add(slot)
    }
    fun addFixedSlot(slot: Slot) {
        Logger.debug(LOG_ID, "Adding fixed slot " + slot + " to queue " + queueId)
        slots.add(slot)
    }
    fun deleteSlot(slotCode: SlotCode) {
        Logger.debug(LOG_ID, "Deleting slot " + slotCode + " from queue " + queueId)
        deletedSlots.add(slotCode)
        slots.removeIf({ it.slotCode == slotCode })
    }
    fun endCurrentSlot() {
        if (slots.isNotEmpty()) {
            Logger.debug(LOG_ID, "Removing current slot " + slots.get(0) + " from queue " + queueId)
            endedSlots.add(slots.get(0).slotCode)
            slots.removeAt(0)
        } else {
            Logger.debug(
                LOG_ID,
                "Could not remove current slot from queue " + queueId + " (queue is empty)"
            )
        }
    }
    fun moveSlotAfterAnother(slotToMove: SlotCode, otherSlot: SlotCode) {
        Logger.debug(
            LOG_ID,
            "Moving slot " + slotToMove + " after slot " + otherSlot + " in queue " + queueId +
                "..."
        )
        // TODO maybe we should use a more stable approach to store the slots in the queue (maybe
        //  multiple lists and spontaneous slots are sorted by index instead of creation time)
        val slot = slots.find { it.slotCode == slotToMove }
        val targetSlot = slots.find { it.slotCode == otherSlot }

        if (slot != null && targetSlot != null) {
            slots.remove(slot)
            val newDate = Date.from(targetSlot.constructorTime.toInstant() + Duration.ofMillis(1))
            val conflictingSlot = slots.find { it.constructorTime == newDate }

            slots.add(slot.copy(constructorTime=newDate))

            if (conflictingSlot != null) {
                // Move the conflicting slots recursively
                moveSlotAfterAnother(conflictingSlot.slotCode, slot.slotCode)
            }
        }
        Logger.debug(LOG_ID, "Slot movement completed")
    }

    /** Replaces a slot in the list with a updated slot */
    private fun replaceSlot(oldSlot:Slot, newSlot:Slot) {
        Logger.debug(
            LOG_ID,
            "(internal): Replacing slot " + oldSlot + " with slot " + newSlot + " in queue " +
                queueId
        )
        val index = slots.indexOf(oldSlot)
        slots.remove(oldSlot)
        slots.add(index, newSlot)
    }

    fun changeAppointmentTime(slotCode: SlotCode, newTime: Date) {
        Logger.debug(
            LOG_ID,
            "Changing appointment time of slot " + slotCode + " to " + newTime + " in queue " +
                queueId
        )
        var oldSlot = slots.find { it.slotCode == slotCode }
        if (oldSlot != null) {
            replaceSlot(oldSlot, oldSlot.copy(constructorTime=newTime))
        } else {
            Logger.debug(LOG_ID, "Failed to change appointment time (slot does not exist)")
        }
    }
    fun updateSlotLength(slotCode: SlotCode, newLength: Duration) {
        Logger.debug(
            LOG_ID,
            "Changing slot length of slot " + slotCode + " to " + newLength + " in queue " + queueId
        )
        var oldSlot = slots.find { it.slotCode == slotCode }
        if (oldSlot != null) {
            replaceSlot(oldSlot, oldSlot.copy(expectedDuration=newLength))
        } else {
            Logger.debug(LOG_ID, "Failed to change slot length (slot does not exist)")
        }
    }

    override fun toString() = slots.toString()
}
