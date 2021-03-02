package edu.kit.outwait.server.protocol

import java.time.Duration
import java.util.Date
import org.json.JSONObject

/**
 * Json wrapper for spontaneous slots.
 *
 * @param obj the json object that should be wrapped.
 * @constructor Creates a new json wrapper from a json object.
 */
class JSONAddSpontaneousSlotWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    /**
     * Secondary constructor with no parameter.
     *
     * Use this to create an empty json wrapper, that will be filled with data.
     */
    constructor() : this(JSONObject())

    /**
     * Setter for the creation time
     *
     * @param time the creation time
     */
    fun setCreationTime(time: Date) {
        obj.put("timeOfCreation", time.getTime())
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
     * Getter for the creation time
     *
     * @return the creation time
     */
    fun getCreationTime(): Date {
        return Date(obj.getLong("timeOfCreation"))
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
