package elite.kit.outwait.networkProtocol

import org.json.JSONObject

class JSONEmptyWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    // secondary constructor creates EmptyWrapper without a JSONObject
    constructor() : this(JSONObject())

}
