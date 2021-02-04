package edu.kit.outwait.server.protocol

import org.json.JSONObject

class JSONResetPasswordWrapper : JSONObjectWrapper {
    constructor() {}
    constructor(obj: JSONObject) {}
    fun setUsername (username: String) {}
    fun getUsername(): String { return "" }
}
