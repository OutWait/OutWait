package elite.kit.outwait.networkProtocol

import elite.kit.outwait.customDataTypes.Mode
import org.json.JSONObject
import elite.kit.outwait.customDataTypes.Preferences
import org.joda.time.Duration

class JSONManagementSettingsWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    constructor(prefs: Preferences) : this(JSONObject()) {
        //TODO Joda hat das in millis aber eigentlich schicken wir in Sekunden?
        // parse and convert values of Preferences Object into timestamp of type Long
        jsonObj.put(DEFAULT_SLOT_DURATION, prefs.defaultSlotDuration.millis)
        jsonObj.put(NOTIFICATION_TIME, prefs.notificationTime.millis)
        jsonObj.put(DELAY_NOTIFICATION_TIME, prefs.delayNotificationTime.millis)

        //TODO Wie verschicken wir den Mode als String oder Int? -> Entsprechend das Enum anpassen
        // jsonObj.put(MODE, prefs.mode)
        jsonObj.put(PRIORITIZATION_TIME, prefs.prioritizationTime.millis)
    }

    fun getPreferences(): Preferences {
        // TODO parse seconds but Joda DateTime takes millis?
        // Parse params for Preferences Object from the JSONObject
        val defaultSlotDuration: Duration = Duration(jsonObj.getLong(DEFAULT_SLOT_DURATION))
        val notificationTime: Duration = Duration(jsonObj.getLong(NOTIFICATION_TIME))
        val delayNotificationTime: Duration = Duration(jsonObj.getLong(DELAY_NOTIFICATION_TIME))
        val prioritizationTime: Duration = Duration(jsonObj.getLong(PRIORITIZATION_TIME))

        // TODO Mode Enum Objekt zurück parsen
        // val mode: Mode = josn

        // Create and return Preferences Object
        return Preferences(defaultSlotDuration, notificationTime, delayNotificationTime,
        prioritizationTime, Mode.ONE)
    }
}
