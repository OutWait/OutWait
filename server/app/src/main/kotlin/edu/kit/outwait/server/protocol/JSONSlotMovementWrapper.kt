package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.slot.SlotCode
import org.json.JSONObject

class JSONSlotMovementWrapper : JSONObjectWrapper {
    constructor() {}
    constructor(obj: JSONObject) {}
    fun setMovedSlot(slotCode: SlotCode) {}
    fun setOtherSlot(slotCode: SlotCode) {}
    fun getMovedSlot(): SlotCode { return SlotCode("") }
    fun getOtherSlot(): SlotCode { return SlotCode("") }
}