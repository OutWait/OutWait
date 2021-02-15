package edu.kit.outwait.server.protocol

import java.time.Duration
import org.json.JSONObject

class JSONChangeSlotDurationWrapper(obj: JSONObject) : JSONSlotCodeWrapper(obj) {
    constructor() : this(JSONObject())
    fun setNewDuration(duration: Duration) {
        obj.put("newDuration", duration.getSeconds())
    }
    fun getNewDuration(): Duration {
        return Duration.ofSeconds(obj.getLong("newDuration"))
    }
}
