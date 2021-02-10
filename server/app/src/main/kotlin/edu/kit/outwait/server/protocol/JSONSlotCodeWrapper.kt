package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.slot.SlotCode
import org.json.JSONObject

open class JSONSlotCodeWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
    fun setSlotCode (slotCode: SlotCode) {}
    fun getSlotCode(): SlotCode { return SlotCode("") }
}
