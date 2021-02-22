package edu.kit.outwait.server.protocol

import java.time.Duration
import java.util.Date
import org.json.JSONObject

class JSONAddSpontaneousSlotWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
    fun setCreationTime(time: Date) {
        obj.put("timeOfCreation", time.getTime())
    }
    fun setDuration(duration: Duration) {
        obj.put("duration", duration.toMillis())
    }
    fun getCreationTime(): Date {
        return Date(obj.getLong("timeOfCreation"))
    }
    fun getDuration(): Duration {
        return Duration.ofMillis(obj.getLong("duration"))
    }
}
