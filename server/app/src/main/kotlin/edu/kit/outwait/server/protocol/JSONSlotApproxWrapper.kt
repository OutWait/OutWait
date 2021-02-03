package edu.kit.outwait.server.protocol

import java.util.Date
import org.json.JSONObject

class JSONSlotApproxWrapper : JSONSlotCodeWrapper {
    constructor() {}
    constructor(obj: JSONObject) {}
    fun setSlotApprox(slotApprox: Date) {}
    fun getSlotApprox(): Date { return Date(0) }
}
