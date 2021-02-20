package edu.kit.outwait.server.protocol

import java.time.Duration
import org.json.JSONObject

class JSONChangeSlotDurationWrapper(obj: JSONObject) : JSONSlotCodeWrapper(obj) {
    constructor() : this(JSONObject())
    fun setNewDuration(duration: Duration) {
        obj.put("newDuration", duration.toMillis())
    }
    fun getNewDuration(): Duration {
        return Duration.ofMillis(obj.getLong("newDuration"))
    }
}
