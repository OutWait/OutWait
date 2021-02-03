package edu.kit.outwait.server.protocol

import org.json.JSONObject

class JSONEmptyWrapper : JSONObjectWrapper {
    constructor() {}
    constructor(obj: JSONObject) {}
}
