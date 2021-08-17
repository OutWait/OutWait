package edu.kit.outwait.networkProtocol

import org.json.JSONObject

/**
 * The JSONObjectWrapper for events, where a slotCode is to be transmitted or received
 *
 * @constructor
 * Primary constructor takes a given JSONObject and wraps it, using the
 * constructor of the base class
 *
 * @param jsonObj The JSONObject that is to be wrapped (containing the slotCode of the received event)
 */
class JSONSlotCodeWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    /**
     * Secondary constructor, takes the data that is to be transmitted and stores it in the
     * (previously empty) JSONObject (of the primary constructor)
     * according to the specified (JSON) protocol
     * @see design documentation (section 9 "Kommunikation App-Server)
     *
     * @param slotCode as String, the slotCode that is to be transmitted
     */
    constructor(slotCode: String) : this(JSONObject()) {
        jsonObj.put(SLOT_CODE, slotCode)
    }

    /**
     * Getter for the slotCode contained in the received JSONObject
     *
     * @return the slotCode that was parsed from the received JSONObject
     */
    fun getSlotCode(): String {
        return jsonObj.getString(SLOT_CODE)
    }

}
