package edu.kit.outwait.server.protocol

import org.json.JSONObject

class JSONCredentialsWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
    fun setUsername (username: String) {}
    fun setPassword (password: String) {}
    fun getUsername(): String { return "" }
    fun getPassword(): String { return "" }
}
