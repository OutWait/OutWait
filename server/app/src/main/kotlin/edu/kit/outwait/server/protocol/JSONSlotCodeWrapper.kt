package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.slot.SlotCode
import org.json.JSONObject

abstract class JSONSlotCodeWrapper : JSONObjectWrapper {
    constructor() {}
    constructor(obj: JSONObject) {}
    fun setSlotCode (slotCode: SlotCode) {}
    fun getSlotCode(): SlotCode { return SlotCode("") }
}
