package elite.kit.outwait.networkProtocol

import org.json.JSONObject

class JSONEmptyWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    // Secondary Constructor erzeugt einen EmptyWrapper ohne übergebenes JSONObject
    constructor() : this(JSONObject())

}
