package elite.kit.outwait.networkProtocol

import org.joda.time.Duration
import org.json.JSONObject

class JSONInvalidRequestWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    /* eigentlich constructor überflüssig, da wir dieses Objekt nur bekommen, nie versenden

    constructor(errorMessage: String) : this(JSONObject()) {
        jsonObj.put(ERROR_MESSAGE, errorMessage)
    }

     */

    fun getErrorMessage(): String {
        return jsonObj.getString(ERROR_MESSAGE)
    }
}
