package edu.kit.outwait.server.protocol

import org.json.JSONObject

class JSONInvalidRequestMessageWrapper : JSONObjectWrapper {
    constructor() {}
    constructor(obj: JSONObject) {}
    fun setMessage(message: String) {}
    fun getMessage(): String { return "" }
}
