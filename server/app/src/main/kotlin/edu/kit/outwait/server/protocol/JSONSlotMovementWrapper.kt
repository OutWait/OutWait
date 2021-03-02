package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.slot.SlotCode
import org.json.JSONObject

/**
 * Json wrapper for slot movement information.
 *
 * @param obj the json object that should be wrapped.
 * @constructor Creates a new json wrapper from a json object.
 */
class JSONSlotMovementWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    /**
     * Secondary constructor with no parameter.
     *
     * Use this to create an empty json wrapper, that will be filled with data.
     */
    constructor() : this(JSONObject())

    /**
     * Setter for the moved slot
     *
     * @param slotCode the moved slot
     */
    fun setMovedSlot(slotCode: SlotCode) {
        obj.put("movedSlot", slotCode.code)
    }

    /**
     * Setter for the orientation slot
     *
     * @param slotCode the orientation slot
     */
    fun setOtherSlot(slotCode: SlotCode) {
        obj.put("otherSlot", slotCode.code)
    }

    /**
     * Getter for the moved slot
     *
     * @return the moved slot
     */
    fun getMovedSlot(): SlotCode {
        return SlotCode(obj.getString("movedSlot"))
    }

    /**
     * Getter for the orientation slot
     *
     * @return the orientation slot
     */
    fun getOtherSlot(): SlotCode {
        return SlotCode(obj.getString("otherSlot"))
    }
}
