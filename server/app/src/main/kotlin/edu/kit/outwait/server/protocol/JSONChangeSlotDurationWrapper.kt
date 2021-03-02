package edu.kit.outwait.server.protocol

import java.time.Duration
import org.json.JSONObject

/**
 * Json wrapper for slot duration updates.
 *
 * @param obj the json object that should be wrapped.
 * @constructor Creates a new json wrapper from a json object.
 */
class JSONChangeSlotDurationWrapper(obj: JSONObject) : JSONSlotCodeWrapper(obj) {
    /**
     * Secondary constructor with no parameter.
     *
     * Use this to create an empty json wrapper, that will be filled with data.
     */
    constructor() : this(JSONObject())

    /**
     * Setter for the new duration
     *
     * @param duration the new duration
     */
    fun setNewDuration(duration: Duration) {
        obj.put("newDuration", duration.toMillis())
    }

    /**
     * Getter for the new duration
     *
     * @return the new duration
     */
    fun getNewDuration(): Duration {
        return Duration.ofMillis(obj.getLong("newDuration"))
    }
}
