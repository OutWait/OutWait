package elite.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

/*
Has no secondary constructor, as we only receive the wrapped JSONObject
 */
class JSONSlotDataWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    fun getSlotCode(): String {
        return jsonObj.getString(SLOT_CODE)
    }

    // TODO parse seconds but Joda DateTime takes millis?
    fun getApproxTime(): DateTime {
        // Creates a DateTime object from a timestamp of type Long
        return DateTime(jsonObj.getLong(APPROX_TIME))
    }

    fun getInstituteName(): String {
        return jsonObj.getString(INSTITUTE_NAME)
    }

    // TODO parse seconds but Joda DateTime takes millis?
    fun getNotificationTime(): Duration {
        // Creates a Duration object from a timestamp of type Long
        return Duration(jsonObj.getLong(NOTIFICATION_TIME))
    }

    // TODO parse seconds but Joda DateTime takes millis?
    fun getDelayNotificationTime(): Duration {
        // Creates a Duration object from a timestamp of type Long
        return Duration(jsonObj.getLong(DELAY_NOTIFICATION_TIME))
    }

}
