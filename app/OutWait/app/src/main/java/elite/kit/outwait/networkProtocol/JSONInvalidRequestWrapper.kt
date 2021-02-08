package elite.kit.outwait.networkProtocol

import org.joda.time.Duration
import org.json.JSONObject

class JSONInvalidRequestWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    constructor(errorMessage: String) : this(JSONObject()) {
        jsonObj.put(ERROR_MESSAGE, errorMessage)
    }

    fun getErrorMessage(): String {
        return jsonObj.getString(ERROR_MESSAGE)
    }
}
