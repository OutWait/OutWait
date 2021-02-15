package edu.kit.outwait.server.protocol

import org.json.JSONObject

class JSONCredentialsWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
    fun setUsername (username: String) {
        obj.put("username", username)
    }
    fun setPassword (password: String) {
        obj.put("password", password)
    }
    fun getUsername(): String {
        return obj.getString("username")
    }
    fun getPassword(): String {
        return obj.getString("password")
    }
}
