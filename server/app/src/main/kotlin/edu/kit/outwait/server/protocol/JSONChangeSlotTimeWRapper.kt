package edu.kit.outwait.server.protocol

import java.util.Date
import org.json.JSONObject

class JSONChangeSlotTimeWrapper(obj: JSONObject) : JSONSlotCodeWrapper(obj) {
    constructor() : this(JSONObject())
    fun setNewTime(time: Date) {}
    fun getNewTime(): Date { return Date(0) }
}
