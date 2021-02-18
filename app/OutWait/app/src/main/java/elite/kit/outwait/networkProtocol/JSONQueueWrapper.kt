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

        val slotOrder = jsonObj.getJSONArray(SLOT_ORDER)
        // Iterate over the JSON Array, retrieving the slotCodes
        // JSON Array does not seem to expose an iterator so we use a for loop
        for (i in 0 until slotOrder.length()) {
            val slotCode: String = slotOrder[i] as String
            order.add(slotCode)
        }

        val spontaneousSlots = jsonObj.getJSONObject(SPONTANEOUS_SLOTS)
        val spontaneous = getSpontSlotsAsList(spontaneousSlots)

        val fixedSlots = jsonObj.getJSONObject(FIXED_SLOTS)
        val fixed = getFixedSlotsAsList(fixedSlots)

        return ReceivedList(currentSlotStartedTime, order, spontaneous, fixed)
    }


    private fun getSpontSlotsAsList(allSpontSlotsObj: JSONObject): List<SpontaneousSlot> {
        val listToReturn: MutableList<SpontaneousSlot> = mutableListOf()

        val keys: MutableIterator<String> = allSpontSlotsObj.keys()

        // Iterate over the nestedJSONObjects in the spontaneousSlots JSONObject
        while (keys.hasNext()) {
            val keyValue = keys.next()
            // get the next one of the nested fixedSlot JSONObjects
            val slotObj = allSpontSlotsObj.getJSONObject(keyValue)

            // Retrieve and parse values of the current slot
            val duration: Duration = Duration(slotObj.getLong(DURATION))
            val slotCode = slotObj.getString(SLOT_CODE)
            // Create new spontaneousSlot Object from parsed values and add to the list
            val spontSlot = SpontaneousSlot(duration, slotCode)
            listToReturn.add(spontSlot)
            keys.remove()
        }
        return listToReturn
    }

    private fun getFixedSlotsAsList(allFixedSlotsObj: JSONObject): List<FixedSlot> {
        val listToReturn: MutableList<FixedSlot> = mutableListOf()

        val keys: MutableIterator<String> = allFixedSlotsObj.keys()

        // Iterate over the nestedJSONObjects in the fixedSlots JSONObject
        while (keys.hasNext()) {
            val keyValue = keys.next()

            // get the next one of the nested fixedSlot JSONObjects
            val slotObj = allFixedSlotsObj.getJSONObject(keyValue)

            // Retrieve and parse the values for the current slot
            val appointmentTime = DateTime(slotObj.getLong(APPOINTMENT_TIME))
            val duration: Duration = Duration(slotObj.getLong(DURATION))
            val slotCode = slotObj.getString(SLOT_CODE)

            // Create new fixedSlot Object from parsed values and add to the list
            val fixSlot = FixedSlot(duration, slotCode, appointmentTime)
            listToReturn.add(fixSlot)
            keys.remove()
        }
        return listToReturn
    }

}
