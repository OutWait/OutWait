package edu.kit.outwait.server.protocol

import org.json.JSONObject

import edu.kit.outwait.server.slot.SlotCode

abstract class JSONSlotCodeWrapper : JSONObjectWrapper {
    constructor() {}
    constructor(obj: JSONObject) {}
    fun setSlotCode (slotCode: SlotCode) {}
    fun getSlotCode(): SlotCode { return SlotCode("") }
}
