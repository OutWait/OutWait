package elite.kit.outwait.networkProtocol

import org.joda.time.Duration
import org.json.JSONObject

class JSONUpdateManagementInformationWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    constructor(slotCode: String, notificationTime: Duration, delayNotificationTime: Duration,
                name: String) : this(JSONObject()) {
        jsonObj.put(SLOT_CODE, slotCode)
        //TODO Joda Einheitn in UNIX Timestamps konvertieren, erst dann in JSONObject putten
    }

    fun getSlotCode(): String {
        return jsonObj.getString(SLOT_CODE)
    }

    fun getNotificationTime(): Duration {
        TODO("JSON String in Joda Einheit konvertieren")
    }

    fun getDelayNotificationTime(): Duration {
        TODO("JSON Srtring in Joda Einheit konvertieren")
    }

    fun getName(): String {
        return jsonObj.getString(NAME)
    }
}
