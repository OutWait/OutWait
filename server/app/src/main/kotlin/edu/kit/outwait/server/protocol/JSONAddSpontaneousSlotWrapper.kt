package edu.kit.outwait.server.protocol

import java.time.Duration
import java.util.Date
import org.json.JSONObject

class JSONAddSpontaneousSlotWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
    fun setCreationTime(time: Date) {
        obj.put("timeOfCreation", time.getTime() / 1000)
    }
    fun setDuration(duration: Duration) {
        obj.put("duration", duration.getSeconds())
    }
    fun getCreationTime(): Date {
        return Date(obj.getLong("timeOfCreation") * 1000)
    }
    fun getDuration(): Duration {
        return Duration.ofSeconds(obj.getLong("duration"))
    }
}
