package edu.kit.outwait.server.protocol

import org.json.JSONObject

abstract class JSONObjectWrapper {
    protected val obj = JSONObject()

    constructor() {}
    constructor(obj: JSONObject) {}
    fun getJSONString(): String { return obj.toString(); }
}
