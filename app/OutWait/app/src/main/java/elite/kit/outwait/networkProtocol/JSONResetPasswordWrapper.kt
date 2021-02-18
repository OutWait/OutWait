package elite.kit.outwait.networkProtocol

import org.json.JSONObject

/*
Has no getters, as we only emit the wrapped JSONObject
 */
class JSONResetPasswordWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    constructor(username: String, password: String) : this(JSONObject()) {
        jsonObj.put(USERNAME, username)
        jsonObj.put(PASSWORD, password)
    }

}
