package elite.kit.outwait.networkProtocol

import elite.kit.outwait.customDataTypes.Mode
import org.json.JSONObject
import elite.kit.outwait.customDataTypes.Preferences
import org.joda.time.Duration
import java.util.*

class JSONManagementSettingsWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    constructor(prefs: Preferences) : this(JSONObject()) {

        // parse and convert values of Preferences Object into timestamp of type Long
        jsonObj.put(DEFAULT_SLOT_DURATION, prefs.defaultSlotDuration.millis)
        jsonObj.put(NOTIFICATION_TIME, prefs.notificationTime.millis)
        jsonObj.put(DELAY_NOTIFICATION_TIME, prefs.delayNotificationTime.millis)
        jsonObj.put(MODE, prefs.mode.toString())
        jsonObj.put(PRIORITIZATION_TIME, prefs.prioritizationTime.millis)
    }

    fun getPreferences(): Preferences {

        // Parse params for Preferences Object from the JSONObject
        val defaultSlotDuration: Duration = Duration(jsonObj.getLong(DEFAULT_SLOT_DURATION))
        val notificationTime: Duration = Duration(jsonObj.getLong(NOTIFICATION_TIME))
        val delayNotificationTime: Duration = Duration(jsonObj.getLong(DELAY_NOTIFICATION_TIME))
        val prioritizationTime: Duration = Duration(jsonObj.getLong(PRIORITIZATION_TIME))

        // parse mode param to enum
        val mode: Mode = if (jsonObj.getString(MODE) == Mode.ONE.toString()) {
            Mode.ONE
        } else {
            Mode.TWO
        }

        // Create and return Preferences Object
        return Preferences(defaultSlotDuration, notificationTime, delayNotificationTime,
        prioritizationTime, mode)
    }
}
