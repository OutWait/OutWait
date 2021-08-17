package edu.kit.outwait.networkProtocol

import edu.kit.outwait.customDataTypes.FixedSlot
import edu.kit.outwait.customDataTypes.ReceivedList
import edu.kit.outwait.customDataTypes.SpontaneousSlot
import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

/**
 * The JSONObjectWrapper for the data of the "updateQueue@M" event, that is to be received
 *
 * @constructor
 * Primary constructor takes a given JSONObject and wraps it, using the
 * constructor of the base class
 *
 * @param jsonObj The JSONObject that is to be wrapped (containing the received data of the event)
 */
class JSONQueueWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    /**
     * Getter for the new Queue, as ReceivedList object, contained in the received JSONObject,
     * parsed according to the specified (JSON) protocol
     * @see design documentation (section 9 "Kommunikation App-Server)
     *
     * @return new Queue as ReceivedList (with the parsed values from the received JSONObject)
     */
    fun getQueue(): ReceivedList {
        // parse the received values for the queue
        val currentSlotStartedTime = DateTime(jsonObj.getLong(CURRENT_SLOT_STARTED_TIME))

        // the following lists can also be empty
        val order: MutableList<String> = mutableListOf()
        val spontaneousSlots: MutableList<SpontaneousSlot> = mutableListOf()
        val fixedSlots: MutableList<FixedSlot> = mutableListOf()

        val parsedSlotOrder = jsonObj.getJSONArray(SLOT_ORDER)
        // Iterate over the JSON Array, retrieving the slotCodes
        // JSON Array does not seem to expose an iterator so we use a for loop
        for (i in 0 until parsedSlotOrder.length()) {
            val slotCode: String = parsedSlotOrder[i] as String
            order.add(slotCode)
        }

        val parsedSpontanSlots = jsonObj.getJSONArray(SPONTANEOUS_SLOTS)
        // Iterate over the JSON Array, retrieving the slot JSONObjects
        // JSON Array does not seem to expose an iterator so we use a for loop
        for (i in 0 until parsedSpontanSlots.length()) {
            val slotJSON: JSONObject = parsedSpontanSlots[i] as JSONObject
            val spontSlot: SpontaneousSlot = getSpontSlotFromJSON(slotJSON)
            spontaneousSlots.add(spontSlot)
        }

        val parsedFixSlots = jsonObj.getJSONArray(FIXED_SLOTS)
        // Iterate over the JSON Array, retrieving the slot JSONObjects
        // JSON Array does not seem to expose an iterator so we use a for loop
        for (i in 0 until parsedFixSlots.length()) {
            val slotJSON: JSONObject = parsedFixSlots[i] as JSONObject
            val fixSlot: FixedSlot = getFixSlotFromJSON(slotJSON)
            fixedSlots.add(fixSlot)
        }

        return ReceivedList(currentSlotStartedTime, order, spontaneousSlots, fixedSlots)
    }

    /**
     * Helper method to create a SpontaneousSlot object from a corresponding JSONObject
     *
     * @param spontJSON from which the attributes of the SpontaneousSlot are to be parsed
     * @return the SpontaneousSlot with the parsed values
     */
    private fun getSpontSlotFromJSON(spontJSON: JSONObject): SpontaneousSlot {
        val duration = Duration(spontJSON.getLong(DURATION))
        val slotCode = spontJSON.getString(SLOT_CODE)
        return SpontaneousSlot(duration, slotCode)
    }

    /**
     * Helper method to create a FixedSlot object from a corresponding JSONObject
     *
     * @param fixJSON from which the attributes of the FixedSlot are to be parsed
     * @return the FixedSlot with the parsed values
     */
    private fun getFixSlotFromJSON(fixJSON: JSONObject): FixedSlot {
        val duration = Duration(fixJSON.getLong(DURATION))
        val slotCode = fixJSON.getString(SLOT_CODE)
        val appointmentTime = DateTime(fixJSON.getLong(APPOINTMENT_TIME))
        return FixedSlot(duration, slotCode, appointmentTime)
    }

}
