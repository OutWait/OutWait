package edu.kit.outwait.networkProtocol

import org.json.JSONObject

/**
 * The JSONObjectWrapper for the data of the "moveSlotAfterAnother@S" event that is to be transmitted
 *
 * @constructor
 * Primary constructor takes a given JSONObject and wraps it, using the
 * constructor of the base class
 *
 * @param jsonObj The JSONObject that is to be wrapped (which will contain the data for this event)
 */
class JSONMoveSlotWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    /**
     * Secondary constructor, takes the data that is to be transmitted and stores it in the
     * (previously empty) JSONObject (of the primary constructor)
     * according to the specified (JSON) protocol
     * @see design documentation (section 9 "Kommunikation App-Server)
     *
     * @param movedSlot as String, specifies the slot that is to be moved
     * @param otherSlot as String, specifies the slot, to which the "movedSlot" should be moved after
     */
    constructor(movedSlot: String, otherSlot: String) : this(JSONObject()) {
        jsonObj.put(MOVED_SLOT, movedSlot)
        jsonObj.put(OTHER_SLOT, otherSlot)
    }

}
