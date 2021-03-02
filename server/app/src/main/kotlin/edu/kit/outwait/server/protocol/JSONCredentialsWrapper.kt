package edu.kit.outwait.server.protocol

import org.json.JSONObject

/**
 * Json wrapper for login credentials.
 *
 * @param obj the json object that should be wrapped.
 * @constructor Creates a new json wrapper from a json object.
 */
class JSONCredentialsWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
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
     * Setter for the password
     *
     * @param password the password
     */
    fun setPassword (password: String) {
        obj.put("password", password)
    }

    /**
     * Getter for the username
     *
     * @return the username
     */
    fun getUsername(): String {
        return obj.getString("username")
    }

    /**
     * Getter for the password
     *
     * @return the password
     */
    fun getPassword(): String {
        return obj.getString("password")
    }
}
