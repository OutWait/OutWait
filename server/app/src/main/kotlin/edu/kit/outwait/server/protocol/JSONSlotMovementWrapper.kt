package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.slot.SlotCode
import org.json.JSONObject

class JSONSlotMovementWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
    fun setMovedSlot(slotCode: SlotCode) {}
    fun setOtherSlot(slotCode: SlotCode) {}
    fun getMovedSlot(): SlotCode { return SlotCode("") }
    fun getOtherSlot(): SlotCode { return SlotCode("") }
}
