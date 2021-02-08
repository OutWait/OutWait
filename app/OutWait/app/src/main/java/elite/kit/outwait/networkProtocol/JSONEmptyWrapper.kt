package elite.kit.outwait.networkProtocol

import org.json.JSONObject

class JSONEmptyWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {
    constructor() : this(JSONObject())

    //TODO Fehlt noch was?
    //Wird bei Events übertragen, die keine Daten mitschicken
}
