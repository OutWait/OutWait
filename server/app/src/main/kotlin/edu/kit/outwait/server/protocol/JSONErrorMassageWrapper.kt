package edu.kit.outwait.server.protocol

import org.json.JSONObject

/**
 * Json wrapper for error messages to the app.
 *
 * @param obj the json object that should be wrapped.
 * @constructor Creates a new json wrapper from a json object.
 */
class JSONErrorMessageWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    /**
     * Secondary constructor with no parameter.
     *
     * Use this to create an empty json wrapper, that will be filled with data.
     */
    constructor() : this(JSONObject())

    /**
     * Setter for the error message
     *
     * @param message the error message
     */
    fun setMessage(message: String) {
        obj.put("errorMessage", message)
    }

    /**
     * Getter for the error message
     *
     * @return the error message
     */
    fun getMessage(): String {
        return obj.getString("errorMessage")
    }
}
