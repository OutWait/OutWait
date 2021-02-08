package elite.kit.outwait.networkProtocol

import org.json.JSONObject
import elite.kit.outwait.customDataTypes.Preferences

class JSONPreferencesWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    constructor(prefs: Preferences) : this(JSONObject()) {
        //TODO Werte aus Joda Klassen parsen in UNIX Timestamp??

        jsonObj.put(DEFAULT_SLOT_DURATION, prefs.defaultSlotDuration)
        jsonObj.put(NOTIFICATION_TIME, prefs.notificationTime)
        jsonObj.put(DELAY_NOTIFICATION_TIME, prefs.delayNotificationTime)
        jsonObj.put(MODE, prefs.mode)
        jsonObj.put(PRIORITIZATION_TIME, prefs.prioritizationTime)
    }

    fun getPreferences(): Preferences {
        TODO("Not yet implemented")
        //aus JSON String Timestamps wieder Joda Time Einheiten machen
    }
}
