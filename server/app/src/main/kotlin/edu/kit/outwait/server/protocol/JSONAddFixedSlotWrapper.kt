package edu.kit.outwait.server.protocol

import java.time.Duration
import java.util.Date
import org.json.JSONObject

class JSONAddFixedSlotWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
    fun setAppointmentTime(time: Date) {
        obj.put("appointmentTime", time.getTime() / 1000)
    }
    fun setDuration(duration: Duration) {
        obj.put("duration", duration.getSeconds())
    }
    fun getAppointmentTime(): Date {
        return Date(obj.getLong("appointmentTime") * 1000)
    }
    fun getDuration(): Duration {
        return Duration.ofSeconds(obj.getLong("duration"))
    }
}
