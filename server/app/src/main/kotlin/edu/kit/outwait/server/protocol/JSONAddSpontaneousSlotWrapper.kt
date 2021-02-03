package edu.kit.outwait.server.protocol

import java.time.Duration
import java.util.Date
import org.json.JSONObject

class JSONAddSpontaneousSlotWrapper : JSONObjectWrapper {
    constructor() {}
    constructor(obj: JSONObject) {}
    fun setCreationTime(time: Date) {}
    fun setDuration(duration: Duration) {}
    fun getCreationTime(): Date { return Date(0) }
    fun getDuration(): Duration { return Duration.ZERO }
}
