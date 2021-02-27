package elite.kit.outwait.networkProtocol

import org.joda.time.Duration
import org.json.JSONObject

/*
Has no secondary constructor, as we only receive the wrapped JSONObject
 */
class JSONErrorMessageWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    fun getErrorMessage(): String {
        return jsonObj.getString(ERROR_MESSAGE)
    }
}
