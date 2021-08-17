package edu.kit.outwait.networkProtocol

import edu.kit.outwait.customDataTypes.Mode
import org.json.JSONObject
import edu.kit.outwait.customDataTypes.Preferences
import org.joda.time.Duration

/**
 * The JSONObjectWrapper for the data of the "changeManagementSettings@S" and the
 * "updateManagementSettings@M" event, that is to be transmitted or respectively received
 *
 * @constructor
 * Primary constructor takes a given JSONObject and wraps it, using the
 * constructor of the base class
 *
 * @param jsonObj The JSONObject that is to be wrapped (containing the data of the received event)
 */
class JSONManagementSettingsWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    /**
     * Secondary constructor, takes the data that is to be transmitted and stores it in the
     * (previously empty) JSONObject (of the primary constructor)
     * according to the specified (JSON) protocol
     * @see design documentation (section 9 "Kommunikation App-Server)
     *
     * @param prefs as Preferences object, containing the requested new management settings
     */
    constructor(prefs: Preferences) : this(JSONObject()) {

        // parse and convert temporal values of Preferences Object into millis timestamps of type Long
        jsonObj.put(DEFAULT_SLOT_DURATION, prefs.defaultSlotDuration.millis)
        jsonObj.put(NOTIFICATION_TIME, prefs.notificationTime.millis)
        jsonObj.put(DELAY_NOTIFICATION_TIME, prefs.delayNotificationTime.millis)
        jsonObj.put(MODE, prefs.mode.toString())
        jsonObj.put(PRIORITIZATION_TIME, prefs.prioritizationTime.millis)
    }

    /**
     * Getter for the new management settings contained in the received JSONObject
     *
     * @return new management settings as Preferences object (parsed from received JSONObject)
     */
    fun getPreferences(): Preferences {

        // Parse params for Preferences Object from the JSONObject
        val defaultSlotDuration = Duration(jsonObj.getLong(DEFAULT_SLOT_DURATION))
        val notificationTime = Duration(jsonObj.getLong(NOTIFICATION_TIME))
        val delayNotificationTime = Duration(jsonObj.getLong(DELAY_NOTIFICATION_TIME))
        val prioritizationTime = Duration(jsonObj.getLong(PRIORITIZATION_TIME))

        // parse mode value (either "one" or "two") to enum
        val mode: Mode = if (jsonObj.getString(MODE) == Mode.ONE.toString()) {
            Mode.ONE
        } else {
            Mode.TWO
        }

        // Create and return Preferences Object
        return Preferences(
            defaultSlotDuration, notificationTime, delayNotificationTime,
            prioritizationTime, mode
        )
    }
}
