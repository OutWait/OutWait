package elite.kit.outwait.networkProtocol

import org.json.JSONObject

class JSONLoginWrapper(jsonObj: JSONObject): JSONObjectWrapper(jsonObj) {

    constructor(username: String, password: String) : this(JSONObject()) {
        jsonObj.put(USERNAME, username)
        jsonObj.put(PASSWORD, password)
    }

    /*
    Eigentlich brauchen wir keine getter, da wir dieses Objekt nur verschicken, nie erhalten

    fun getUsername(): String {
        return jsonObj.getString(USERNAME)
    }

    fun getPassword(): String {
        return jsonObj.getString(PASSWORD)
    }

     */

}
