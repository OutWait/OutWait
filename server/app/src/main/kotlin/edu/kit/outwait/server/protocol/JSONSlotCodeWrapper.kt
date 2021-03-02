package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.slot.SlotCode
import org.json.JSONObject

/**
 * Basic json wrapper for a slot code.
 *
 * Other json wrappers with slot codes can inherit from this.
 *
 * @param obj the json object that should be wrapped.
 * @constructor Creates a new json wrapper from a json object.
 */
open class JSONSlotCodeWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    /**
     * Secondary constructor with no parameter.
     *
     * Use this to create an empty json wrapper, that will be filled with data.
     */
    constructor() : this(JSONObject())

    /**
     * Setter for the slot code
     *
     * @param slotCode the slot code
     */
    fun setSlotCode (slotCode: SlotCode) {
        obj.put("slotCode", slotCode.code)
    }

    /**
     * Getter for the slot code
     *
     * @return the slot code
     */
    fun getSlotCode(): SlotCode {
        return SlotCode(obj.getString("slotCode"))
    }
}
