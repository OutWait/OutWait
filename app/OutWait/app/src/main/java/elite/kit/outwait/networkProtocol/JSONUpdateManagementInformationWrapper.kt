package elite.kit.outwait.networkProtocol

import org.joda.time.Duration
import org.json.JSONObject

class JSONUpdateManagementInformationWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    /*
    TODO Brauchen wir wirklich den secondary Konstruktor,
     da wir dieses Objekt nur erhalten, nie selbst verschicken

    constructor(slotCode: String, notificationTime: Duration, delayNotificationTime: Duration,
                name: String) : this(JSONObject()) {
        jsonObj.put(SLOT_CODE, slotCode)
    }

     */

    fun getSlotCode(): String {
        return jsonObj.getString(SLOT_CODE)
    }

    fun getNotificationTime(): Duration {

        // Creates a Duration object from a timestamp of type Long
        return Duration(jsonObj.getLong(NOTIFICATION_TIME))
    }

    fun getDelayNotificationTime(): Duration {

        // Creates a Duration object from a timestamp of type Long
        return Duration(jsonObj.getLong(DELAY_NOTIFICATION_TIME))
    }

    fun getName(): String {
        return jsonObj.getString(NAME)
    }
}
