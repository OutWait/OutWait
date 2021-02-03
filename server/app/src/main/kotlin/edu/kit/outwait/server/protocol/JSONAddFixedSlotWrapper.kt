package edu.kit.outwait.server.protocol

import java.time.Duration
import java.util.Date
import org.json.JSONObject

class JSONAddFixedSlotWrapper : JSONObjectWrapper {
    constructor() {}
    constructor(obj: JSONObject) {}
    fun setAppointmentTime(time: Date) {}
    fun setDuration(duration: Duration) {}
    fun getAppointmentTime(): Date { return Date(0) }
    fun getDuration(): Duration { return Duration.ZERO }
}
