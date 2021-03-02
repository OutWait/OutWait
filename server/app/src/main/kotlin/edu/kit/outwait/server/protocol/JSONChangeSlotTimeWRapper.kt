package edu.kit.outwait.server.protocol

import java.util.Date
import org.json.JSONObject

/**
 * Json wrapper for slot time updated.
 *
 * @param obj the json object that should be wrapped.
 * @constructor Creates a new json wrapper from a json object.
 */
class JSONChangeSlotTimeWrapper(obj: JSONObject) : JSONSlotCodeWrapper(obj) {
    /**
     * Secondary constructor with no parameter.
     *
     * Use this to create an empty json wrapper, that will be filled with data.
     */
    constructor() : this(JSONObject())

    /**
     * Setter for the new time
     *
     * @param time the new time
     */
    fun setNewTime(time: Date) {
        obj.put("newTime", time.getTime())
    }

    /**
     * Getter for the new time
     *
     * @return the new time
     */
    fun getNewTime(): Date {
        return Date(obj.getLong("newTime"))
    }
}
