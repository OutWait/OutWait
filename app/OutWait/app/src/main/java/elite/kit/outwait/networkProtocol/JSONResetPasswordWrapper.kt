package elite.kit.outwait.networkProtocol

import org.json.JSONObject

class JSONResetPasswordWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    constructor(username: String) : this(JSONObject()) {
        jsonObj.put(USERNAME, username)
    }

    /*
    Eigentlich brauchen wir getter nicht, da wir das Objekt nur versenden, nie erhalten
     */
}
