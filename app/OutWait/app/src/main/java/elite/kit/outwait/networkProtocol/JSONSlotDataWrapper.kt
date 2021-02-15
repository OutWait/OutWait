package elite.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

class JSONSlotDataWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    fun getSlotCode(): String {
        return jsonObj.getString(SLOT_CODE)
    }

    fun getApproxTime(): DateTime {
        // Creates a DateTime object from a timestamp of type Long
        return DateTime(jsonObj.getLong(APPROX_TIME))
    }

    fun getInstituteName(): String {
        return jsonObj.getString(INSTITUTE_NAME)
    }

    fun getNotificationTime(): Duration {
        // Creates a Duration object from a timestamp of type Long
        return Duration(jsonObj.getLong(NOTIFICATION_TIME))
    }

    fun getDelayNotificationTime(): Duration {
        // Creates a Duration object from a timestamp of type Long
        return Duration(jsonObj.getLong(DELAY_NOTIFICATION_TIME))
    }

}
