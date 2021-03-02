package edu.kit.outwait.server.protocol

import org.json.JSONObject

/**
 * Json wrapper for a reset password request.
 *
 * @param obj the json object that should be wrapped.
 * @constructor Creates a new json wrapper from a json object.
 */
class JSONResetPasswordWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    /**
     * Secondary constructor with no parameter.
     *
     * Use this to create an empty json wrapper, that will be filled with data.
     */
    constructor() : this(JSONObject())

    /**
     * Setter for the username
     *
     * @param username the username
     */
    fun setUsername (username: String) {
        obj.put("username", username)
    }

    /**
     * Getter for the username
     *
     * @return the username
     */
    fun getUsername(): String {
        return obj.getString("username")
    }
}
