package edu.kit.outwait.server.protocol

import java.time.Duration
import java.util.Date
import org.json.JSONObject

class JSONAddFixedSlotWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
    fun setAppointmentTime(time: Date) {
        obj.put("appointmentTime", time.getTime())
    }
    fun setDuration(duration: Duration) {
        obj.put("duration", duration.toMillis())
    }
    fun getAppointmentTime(): Date {
        return Date(obj.getLong("appointmentTime"))
    }
    fun getDuration(): Duration {
        return Duration.ofMillis(obj.getLong("duration"))
    }
}
