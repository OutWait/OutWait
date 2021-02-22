package elite.kit.outwait.networkProtocol

import elite.kit.outwait.customDataTypes.FixedSlot
import elite.kit.outwait.customDataTypes.ReceivedList
import elite.kit.outwait.customDataTypes.SpontaneousSlot
import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

/*
Has no secondary constructor, as we only receive the wrapped JSONObject
 */
class JSONQueueWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    fun getQueue(): ReceivedList {
        val currentSlotStartedTime: DateTime = DateTime(jsonObj.getLong(CURRENT_SLOT_STARTED_TIME))
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

    private fun getSpontSlotFromJSON(spontJSON: JSONObject): SpontaneousSlot {
        val duration = Duration(spontJSON.getLong(DURATION))
        val slotCode = spontJSON.getString(SLOT_CODE)
        return SpontaneousSlot(duration, slotCode)
    }

    private fun getFixSlotFromJSON(fixJSON: JSONObject): FixedSlot {
        val duration = Duration(fixJSON.getLong(DURATION))
        val slotCode = fixJSON.getString(SLOT_CODE)
        val appointmentTime = DateTime(fixJSON.getLong(APPOINTMENT_TIME))
        return FixedSlot(duration, slotCode, appointmentTime)
    }

}
