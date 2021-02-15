package edu.kit.outwait.server.protocol

import org.json.JSONObject

class JSONInvalidRequestMessageWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
    fun setMessage(message: String) {
        obj.put("errorMessage", message)
    }
    fun getMessage(): String {
        return obj.getString("errorMessage")
    }
}
