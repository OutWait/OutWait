package edu.kit.outwait.server.protocol

import org.json.JSONObject

class JSONCredentialsWrapper : JSONObjectWrapper {
    constructor() {}
    constructor(obj: JSONObject) {}
    fun setUsername (username: String) {}
    fun setPassword (password: String) {}
    fun getUsername(): String { return "" }
    fun getPassword(): String { return "" }
}
