package edu.kit.outwait.server.protocol

import java.time.Duration
import org.json.JSONObject

class JSONChangeSlotDurationWrapper : JSONSlotCodeWrapper {
    constructor() {}
    constructor(obj: JSONObject) {}
    fun setNewDuration(duration: Duration) {}
    fun getNewDuration(): Duration { return Duration.ZERO }
}
