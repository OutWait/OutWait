package edu.kit.outwait.server.protocol

import org.json.JSONObject

class JSONInvalidRequestMessageWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
    fun setMessage(message: String) {}
    fun getMessage(): String { return "" }
}
