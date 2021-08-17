package edu.kit.outwait.networkProtocol

import org.joda.time.Duration
import org.json.JSONObject

/**
 * The JSONObjectWrapper for the data of the "changeSlotDuration@S" event that is to be transmitted
 *
 * @constructor
 * Primary constructor takes a given JSONObject and wraps it, using the
 * constructor of the base class
 *
 * @param jsonObj The JSONObject that is to be wrapped (which will contain the data for this event)
 */
class JSONChangeSlotDurationWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    /**
     * Secondary constructor, takes the data that is to be transmitted and stores it in the
     * (previously empty) JSONObject (of the primary constructor)
     * according to the specified (JSON) protocol
     * @see design documentation (section 9 "Kommunikation App-Server)
     *
     * @param slotCode as String, specifies the slot, whose duration should be changed
     * @param newDuration as Duration object, specifies the requested new duration of the given slot
     */
    constructor(slotCode: String, newDuration: Duration) : this(JSONObject()) {

        val timeStampDuration: Long = newDuration.millis

        jsonObj.put(SLOT_CODE, slotCode)
        jsonObj.put(NEW_DURATION, timeStampDuration)
    }

}
