package edu.kit.outwait.server.protocol

import org.json.JSONObject

class JSONResetPasswordWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
    fun setUsername (username: String) {}
    fun getUsername(): String { return "" }
}
