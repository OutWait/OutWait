package edu.kit.outwait.waitingQueue.gravityQueue

import edu.kit.outwait.customDataTypes.FixedSlot
import edu.kit.outwait.customDataTypes.ReceivedList
import edu.kit.outwait.customDataTypes.SpontaneousSlot
import edu.kit.outwait.waitingQueue.timeSlotModel.Pause
import edu.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import org.joda.time.DateTime
import org.joda.time.Interval

/**
 * This class has only one public method ([receivedListToTimeSlotList]) that converts
 * a received list to a time slot list with help of the gravity algorithm.
 * More details in the Method description.
 *
 */
class GravityQueueConverter {

    /**
     * Converts a received list object (the list we received from the server) to a
     * list of time slots and maps the locally stored auxiliary identifiers to the slots.
     * Since the received list does not contain starting and ending points of time
     * for the individual slots, this method applies the gravity algorithm (see design
     * document, chapter 10.1) to determine those points in time.
     *
     * @param receivedList the slot list we received from the server
     * @param auxiliaryIdentifiers Map with slot codes as keys and their auxiliary
     * identifiers as values
     * @return list of time slots: Each client slot with its calculated interval, all free
     * spaces between two client slots are filled with one pause slot.
     */
    fun receivedListToTimeSlotList(
        receivedList: ReceivedList,
        auxiliaryIdentifiers: Map<String, String>,
    ): List<TimeSlot> {
        //convert received list to gravity queue and map auxiliary identifiers
        val gravityQueue = receivedListToGravityQueue(receivedList, auxiliaryIdentifiers)
        val currentSlotStartedTime = receivedList.currentSlotStartedTime
        //apply gravity algorithm and fill free space with pauses
        return gravitySlotListToTimeSlotList(gravityQueue, currentSlotStartedTime, DateTime.now())
    }

    private fun receivedListToGravityQueue(
        receivedList: ReceivedList,
        auxiliaryIdentifiers: Map<String, String>
    ): List<ClientGravitySlot> {
        val gravityList = mutableListOf<ClientGravitySlot>()
        for (code in receivedList.order) {
            /*
            a slot can either be fixed or spontaneous, but not both
             */
            if (isSpontaneous(code, receivedList)) {
                gravityList.add(toGravitySlot(
                    getSpontaneous(code, receivedList)!!,
                    auxiliaryIdentifiers)
                )
            }
            if (isFixed(code, receivedList)) {
                gravityList.add(toGravitySlot(
                    getFixed(code, receivedList)!!,
                    auxiliaryIdentifiers)
                )
            }
        }
        return gravityList
    }


    //convert spontaneous slot from received list to spontaneous gravity slot and find its aux
    private fun toGravitySlot(
        slot: SpontaneousSlot,
        auxiliaryIdentifiers: Map<String, String>
    ) = SpontaneousGravitySlot(
        slot.slotCode,
        slot.duration,
        getAuxiliary(slot.slotCode, auxiliaryIdentifiers)
    )

    //convert spontaneous slot from received list to spontaneous gravity slot and find its aux
    private fun toGravitySlot(
        slot: FixedSlot,
        auxiliaryIdentifiers: Map<String, String>
    ) = FixedGravitySlot(
        slot.slotCode,
        slot.duration,
        slot.appointmentTime,
        getAuxiliary(slot.slotCode, auxiliaryIdentifiers)
    )

    /*
    for given slot code, search aux and return it or return empty string when not in list.
     */
    private fun getAuxiliary(code: String, auxiliaryIdentifiers: Map<String, String>): String {
        var aux = ""
        if (auxiliaryIdentifiers.containsKey(code)) {
            aux = auxiliaryIdentifiers[code]!!
        }
        return aux
    }

    /*
    returns fixed slot if the received list contains the fixed slot and elsewise null
     */
    private fun getFixed(code: String, receivedList: ReceivedList): FixedSlot? {
        for (slot in receivedList.fixed) {
            if (slot.slotCode == code) {
                return slot
            }
        }
        return null
    }

    /*
    returns spontaneous slot if the received list contains the fixed slot and elsewise null
    */
    private fun getSpontaneous(code: String, receivedList: ReceivedList): SpontaneousSlot? {
        for (slot in receivedList.spontaneous) {
            if (slot.slotCode == code) {
                return slot
            }
        }
        return null
    }

    private fun isFixed(code: String, receivedList: ReceivedList) =
        (getFixed(code, receivedList) !== null)

    private fun isSpontaneous(code: String, receivedList: ReceivedList) =
        (getSpontaneous(code, receivedList) !== null)

    /*
     * applies the gravity algorithm and fills free time between client time slots with pause
     * slots. Takes not only the current slot started time, but also the current time
     * (as a parameter for testability) to check if the current slot is already running
     * longer than planned.
     */
    private fun gravitySlotListToTimeSlotList(
        gravitySlotList: List<ClientGravitySlot>,
        currentSlotStartedTime: DateTime,
        now: DateTime,
    ): List<TimeSlot> {
        //When the list is empty the algorithm does not have to be executed.
        if (gravitySlotList.isEmpty()) return listOf<TimeSlot>()

        //Make a dummy so that the first gravity slot already has a predecessor
        val firstTimeSlotDummy =
            if (currentSlotStartedTime + gravitySlotList.first().duration < now) {
                //Current slot is delaying
                Pause(Interval(
                    now - gravitySlotList.first().duration,
                    now - gravitySlotList.first().duration
                ))
            } else {
                //Current slot is not delaying
                Pause(Interval(currentSlotStartedTime, currentSlotStartedTime))
            }

        //push the dummy to the front of the list so that we can start the iteration
        val timeSlotList = mutableListOf<TimeSlot>(firstTimeSlotDummy)

        //gravity algorithm
        for (slot in gravitySlotList) {
            /*
            the magic happens in toClientTimeSlot with dynamic polymorphism.
            Each slot calculates its interval itself.
             */
            timeSlotList.add(slot.toClientTimeSlot(timeSlotList.last()))
        }

        //now the dummy can be removed
        timeSlotList.remove(firstTimeSlotDummy)

        //insert pauses in free time between slots
        val listWithPauses = mutableListOf<TimeSlot>()
        var i: Int = 0
        while (i < timeSlotList.size) {
            listWithPauses.add(timeSlotList[i])
            if (i != timeSlotList.lastIndex &&
                timeSlotList[i].interval.end < timeSlotList[i + 1].interval.start) {
                timeSlotList.add(
                    i + 1, Pause(Interval(
                        timeSlotList[i].interval.end,
                        timeSlotList[i + 1].interval.start
                    ))
                )
            }
            i++
        }
        return listWithPauses
    }
}
