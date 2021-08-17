package edu.kit.outwait.customDataTypes

import org.joda.time.DateTime
import org.joda.time.Duration
import org.junit.Assert.*

/**
 * Has static method that helps to create ReceivedList Test instances
 *
 */
class ReceivedListUtil {
    companion object {
        /**
         * Creates a received list with Slots with the Slot Codes
         * SlotCode1, SlotCode2, SlotCode3...
         * All Slots are spontaneous with 30 Minutes
         * @param nrOfSlots number of slots the list shall contain
         */
         fun prepareReceivedList(nrOfSlots: Int): ReceivedList{
            val slotCodes = mutableListOf<String>()
            val slots = mutableListOf<SpontaneousSlot>()
            for (i in 1..nrOfSlots){
                val code = "SlotCode$i"
                slotCodes += code
                slots += SpontaneousSlot(Duration(30), code)
            }
            return ReceivedList(
                DateTime(),
                slotCodes,
                slots,
                listOf()
            )
        }
    }
}
