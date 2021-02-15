package edu.kit.outwait.server.protocol

import java.util.Date
import org.json.JSONObject

class JSONChangeSlotTimeWrapper(obj: JSONObject) : JSONSlotCodeWrapper(obj) {
    constructor() : this(JSONObject())
    fun setNewTime(time: Date) {
        obj.put("newTime", time.getTime() / 1000)
    }
    fun getNewTime(): Date {
        return Date(obj.getLong("newTime") * 1000)
    }
}
