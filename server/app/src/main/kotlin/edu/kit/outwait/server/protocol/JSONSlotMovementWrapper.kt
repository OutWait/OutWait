package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.slot.SlotCode
import org.json.JSONObject

class JSONSlotMovementWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
    fun setMovedSlot(slotCode: SlotCode) {
        obj.put("movedSlot", slotCode.code)
    }
    fun setOtherSlot(slotCode: SlotCode) {
        obj.put("otherSlot", slotCode.code)}
    fun getMovedSlot(): SlotCode {
        return SlotCode(obj.getString("movedSlot"))
    }
    fun getOtherSlot(): SlotCode {
        return SlotCode(obj.getString("otherSlot"))
    }
}
