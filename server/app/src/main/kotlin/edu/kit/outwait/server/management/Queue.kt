package edu.kit.outwait.server.management

import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.core.Logger
import edu.kit.outwait.server.slot.Priority
import edu.kit.outwait.server.slot.Slot
import edu.kit.outwait.server.slot.SlotCode
import java.time.Duration
import java.util.Date
import org.json.JSONObject

/**
 * Representation of a queue.
 *
 * Implements the operations and algorithms regarding the queue. Is only created on demand, which
 * means while a transaction is performed or to send the initial queue to the management.
 *
 * @property queueId the id of the queue.
 * @param databaseWrapper the DB to load the queue.
 * @constructor Loads the queue from the DB.
 */
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

    /**
     * The main update algorithm.
     *
     * Calculates the order an positioning of all slots in the queue. Call this method after a
     * change of the slots in queue. The algorithm uses the [prioritizationTime] to properly
     * calculate the queue.
     *
     * @param prioritizationTime the prioritization settings of ManagementSettings.
     */
    fun updateQueue(prioritizationTime: Duration) {
        Logger.debug(LOG_ID, "Updating queue " + queueId)
        delayChangeTime = null

        val delayTimeBuffer = Duration.ofSeconds(20) // The time buffer on top of an overdue slot

        // Remove outdated slots (to keep the db clean)
        slots
            .filter {
                it.constructorTime.toInstant().isBefore(Date().toInstant() - Duration.ofHours(48))
            }
            .forEach {
                Logger.debug(LOG_ID, "Remove outdated slot " + it.slotCode)
                deletedSlots.add(it.slotCode)
            }
        deletedSlots.forEach { slotCode -> slots.removeIf { slot -> slot.slotCode == slotCode } }

        // Check if queue is not empty (the algorithm below requires at least one slot)
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
            if (newQueue.isNotEmpty()) {
                val endTime = newQueue[0].approxTime.toInstant() + newQueue[0].expectedDuration
                if (endTime.isAfter(Date().toInstant())) {
                    // Slot has started, but expected end has not been reached
                    endTime
                } else {
                    // Slot has started and expected end has been reached

                    // Update the length of the slot to match the gravity-queue protocol
                    // requirements
                    newQueue[0] =
                        newQueue[0]
                            .copy(
                                expectedDuration =
                                    Duration.ofMillis(
                                        Date().getTime() - newQueue[0].approxTime.getTime() +
                                            delayTimeBuffer.toMillis()
                                    )
                            )

                    // Return the position of the line
                    Date().toInstant()
                }
            } else {
                // No slot has started jet
                Date().toInstant()
            }

        // Construct the new queue using a sweep line-like algorithm
        Logger.debug(LOG_ID, "Running queue update algorithm...")
        Logger.debug(LOG_ID, "=========== QUEUE UPDATE ===========")
        Logger.debug(LOG_ID, "Start queue at " + Date.from(line))
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
                remainingPrioritizationBuffer.negated().isNegative()
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
                Logger.debug(
                    LOG_ID,
                    "Chose fix slot " + chosenSlot.slotCode + " at " + chosenSlot.approxTime
                )
                newQueue.add(chosenSlot)
                fixSlots.removeAt(0)

                line = chosenSlot.approxTime.toInstant() + chosenSlot.expectedDuration
            } else {
                val chosenSlot =
                    nextSpontaneous.copy(
                        approxTime = Date.from(line),
                        priority =
                            if (remainingPrioritizationBuffer.negated().isNegative())
                                Priority.NORMAL
                            else
                                Priority.URGENT
                    )
                Logger.debug(
                    LOG_ID,
                    "Chose spontaneous slot " + chosenSlot.slotCode + " at " + chosenSlot.approxTime
                )
                newQueue.add(chosenSlot)
                spontaneousSlots.removeAt(0)

                line += chosenSlot.expectedDuration
            }
        }

        // Add remaining slots
        if (spontaneousSlots.isNotEmpty()) {
            Logger.debug(LOG_ID, "Add remaining spontaneous slots:")
            for (slot in spontaneousSlots) {
                val newSlot = slot.copy(approxTime = Date.from(line))
                newQueue.add(newSlot)
                Logger.debug(LOG_ID, "Slot " + newSlot.slotCode + " at " + newSlot.approxTime)
                line += newSlot.expectedDuration
            }
        } else if (fixSlots.isNotEmpty()) {
            Logger.debug(LOG_ID, "Add remaining fix slots:")
            for (slot in fixSlots) {
                val newSlot =
                    slot.copy(
                        approxTime =
                            if (line.isAfter(slot.constructorTime.toInstant()))
                                Date.from(line)
                            else
                                slot.constructorTime
                    )
                newQueue.add(newSlot)
                Logger.debug(LOG_ID, "Slot " + newSlot.slotCode + " at " + newSlot.approxTime)
                line = newSlot.approxTime.toInstant() + newSlot.expectedDuration
            }
        }

        // Store delay time
        if (newQueue.isNotEmpty()) {
            val nextSlotEnd = newQueue[0].approxTime.toInstant() + newQueue[0].expectedDuration

            delayChangeTime =
                Date.from(
                    if (nextSlotEnd.isBefore(Date().toInstant()))
                        Date().toInstant()
                    else
                        nextSlotEnd
                )
        }

        Logger.debug(LOG_ID, "Queue update algorithm finished. New queue: " + newQueue)
        Logger.debug(LOG_ID, "========= QUEUE UPDATE END =========")
        Logger.debug(LOG_ID, "Next delay change time: " + delayChangeTime)

        // Finalize the queue
        slots = newQueue
    }

    /**
     * Returns the time of the next not-trivial change of the queue.
     *
     * Those changes may happen due to prioritization of slots due to other delayed slots.
     * "Not-Trivial" means it is not a simple postponement of spontaneous slots, but the queue is
     * re-ordered.
     *
     * @return The date a which the next non-trivial change happens.
     */
    fun calculateNextDelayChange(): Date? {
        return delayChangeTime
    }

    /**
     * Saves the state of the queue to the database.
     *
     * This includes all deleted or ended slot (of this transaction).
     *
     * @param databaseWrapper the DB into which the queue is saved.
     * @return False when the queue couldn't be saved
     */
    fun storeToDB(databaseWrapper: DatabaseWrapper): Boolean {
        Logger.debug(LOG_ID, "Storing queue " + queueId + " into the DB")
        var success = true
        if (!databaseWrapper.saveSlots(slots, queueId)) success = false
        deletedSlots.forEach { if (!databaseWrapper.deleteSlot(it)) success = false }
        endedSlots.forEach { if (!databaseWrapper.endSlot(it)) success = false }
        deletedSlots.clear()
        endedSlots.clear()
        return success
    }

    /**
     * Saves the state of the queue into a json object.
     *
     * @param json the json object into which the data should be written.
     */
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

    /**
     * Add a new slot the the queue.
     *
     * Call updateQueue after this method call.
     *
     * @param slot the new slot to add.
     */
    fun addSlot(slot: Slot) {
        Logger.debug(LOG_ID, "Adding new slot " + slot + " to " + queueId)
        slots.add(slot)
    }

    /**
     * Delete a slot from the queue.
     *
     * Call updateQueue after this method call.
     *
     * @param slotCode the code of the slot to delete.
     */
    fun deleteSlot(slotCode: SlotCode) {
        Logger.debug(LOG_ID, "Deleting slot " + slotCode + " from queue " + queueId)
        deletedSlots.add(slotCode)
        slots.removeIf({ it.slotCode == slotCode })
    }

    /**
     * Ends the current slot (the very first in the queue)
     *
     * Call updateQueue after this method call.
     */
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

    /**
     * Move one slot after another slot.
     *
     * Call updateQueue after this method call. It is not possible to move a slot before the first
     * slot (for various reasons).
     *
     * @param slotToMove the code of the slot that should be moved.
     * @param otherSlot the slot before which the first slot should be moved.
     */
    fun moveSlotAfterAnother(slotToMove: SlotCode, otherSlot: SlotCode) {
        Logger.debug(
            LOG_ID,
            "Moving slot " + slotToMove + " after slot " + otherSlot + " in queue " + queueId +
                "..."
        )
        val slot = slots.find { it.slotCode == slotToMove }
        val targetSlot = slots.find { it.slotCode == otherSlot }

        if (slot != null && targetSlot != null) {
            slots.remove(slot)
            val newDate = Date.from(targetSlot.constructorTime.toInstant() + Duration.ofMillis(1))
            val conflictingSlot = slots.find { it.constructorTime == newDate }

            slots.add(slot.copy(constructorTime=newDate, approxTime=newDate))

            if (conflictingSlot != null) {
                // Move the conflicting slots recursively
                moveSlotAfterAnother(conflictingSlot.slotCode, slot.slotCode)
            }
        }
        Logger.debug(LOG_ID, "Slot movement completed")
    }

    /**
     * Replaces a slot in the list with a updated slot .
     *
     * This method is used internally to serve various slot update tasks.
     *
     * @param oldSlot the slot that should be replaced.
     * @param newSlot the new slot that should take the place of the old slot.
     */
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

    /**
     * Updates the appointment time of a slot.
     *
     * Call updateQueue after this method call.
     *
     * @param slotCode the code of the slot to update.
     * @param newTime the new appointment time of the slot.
     */
    fun changeAppointmentTime(slotCode: SlotCode, newTime: Date) {
        Logger.debug(
            LOG_ID,
            "Changing appointment time of slot " + slotCode + " to " + newTime + " in queue " +
                queueId
        )
        var oldSlot = slots.find { it.slotCode == slotCode }
        if (oldSlot != null) {
            replaceSlot(oldSlot, oldSlot.copy(constructorTime=newTime, approxTime=newTime))
        } else {
            Logger.debug(LOG_ID, "Failed to change appointment time (slot does not exist)")
        }
    }

    /**
     * Updates the length/expected duration of a slot.
     *
     * Call updateQueue after this method call.
     *
     * @param slotCode the code of the slot to update.
     * @param newTime the new length/expected duration of the slot.
     */
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

    /**
     * Returns the string representation of the queue.
     *
     * Used for debugging purposes.
     *
     * @return The string representation of the queue.
     */
    override fun toString() = slots.toString()
}
