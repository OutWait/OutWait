package edu.kit.outwait.server.protocol

import org.json.JSONObject

class JSONEmptyWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
}
