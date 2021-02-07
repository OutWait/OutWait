package edu.kit.outwait.server.protocol

import java.time.Duration
import java.util.Date
import org.json.JSONObject

class JSONAddFixedSlotWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
    fun setAppointmentTime(time: Date) {}
    fun setDuration(duration: Duration) {}
    fun getAppointmentTime(): Date { return Date(0) }
    fun getDuration(): Duration { return Duration.ZERO }
}
