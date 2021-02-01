package edu.kit.outwait.protocol

abstract class JSONObjectWrapper {
    protected object: JSONObject

    constructor() {}
    constructor(object: JSONObject) {}
    fun getJSONString(): String { return object.toString(); }

}
