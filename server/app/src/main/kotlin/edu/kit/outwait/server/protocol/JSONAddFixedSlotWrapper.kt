package edu.kit.outwait.server.protocol

import java.time.Duration
import java.util.Date
import org.json.JSONObject

/**
 * Json wrapper for fix slots.
 *
 * @param obj the json object that should be wrapped.
 * @constructor Creates a new json wrapper from a json object.
 */
class JSONAddFixedSlotWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    /**
     * Secondary constructor with no parameter.
     *
     * Use this to create an empty json wrapper, that will be filled with data.
     */
    constructor() : this(JSONObject())

    /**
     * Setter for the appointment time
     *
     * @param time the appointment time
     */
    fun setAppointmentTime(time: Date) {
        obj.put("appointmentTime", time.getTime())
    }

    /**
     * Setter for the duration
     *
     * @param duration the duration
     */
    fun setDuration(duration: Duration) {
        obj.put("duration", duration.toMillis())
    }

    /**
     * Getter for the appointment time
     *
     * @return the appointment time
     */
    fun getAppointmentTime(): Date {
        return Date(obj.getLong("appointmentTime"))
    }

    /**
     * Getter for the duration
     *
     * @return the duration
     */
    fun getDuration(): Duration {
        return Duration.ofMillis(obj.getLong("duration"))
    }
}
