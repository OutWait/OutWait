package edu.kit.outwait.protocol

class JSONSlotMovementWrapper : JSONObjectWrapper {
    constructor() {}
    constructor(object: JSONObject) {}
    fun setMovedSlot(slotCode: SlotCode) {}
    fun setOtherSlot(slotCode: SlotCode) {}
    fun getMovedSlot(): SlotCode {}
    fun getOtherSlot(): SlotCode {}
}
