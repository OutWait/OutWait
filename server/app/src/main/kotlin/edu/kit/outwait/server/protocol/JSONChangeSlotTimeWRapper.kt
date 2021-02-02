package edu.kit.outwait.server.protocol

import java.util.Date
import org.json.JSONObject

class JSONChangeSlotTimeWrapper : JSONSlotCodeWrapper {
    constructor() {}
    constructor(obj: JSONObject) {}
    fun setNewTime(time: Date) {}
    fun getNewTime(): Date { return Date(0) }
}
