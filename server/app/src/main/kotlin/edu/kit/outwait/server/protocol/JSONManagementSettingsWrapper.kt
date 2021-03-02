package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.management.ManagementSettings
import edu.kit.outwait.server.management.Mode
import java.time.Duration
import org.json.JSONObject

/**
 * Json wrapper for management settings.
 *
 * @param obj the json object that should be wrapped.
 * @constructor Creates a new json wrapper from a json object.
 */
class JSONManagementSettingsWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    /**
     * Secondary constructor with no parameter.
     *
     * Use this to create an empty json wrapper, that will be filled with data.
     */
    constructor() : this(JSONObject())

    /**
     * Setter for the management settings
     *
     * @param settings the management settings
     */
    fun setSettings(settings: ManagementSettings) {
        obj.put("defaultSlotDuration", settings.defaultSlotDuration.toMillis())
        obj.put("notificationTime", settings.notificationTime.toMillis())
        obj.put("delayNotificationTime", settings.delayNotificationTime.toMillis())
        obj.put("mode", if (settings.mode == Mode.ONE) "one" else "two")
        obj.put("prioritizationTime", settings.prioritizationTime.toMillis())
    }

    /**
     * Getter for the management settings
     *
     * @return the management settings
     */
    fun getSettings(): ManagementSettings {
        return ManagementSettings(
            if (obj.getString("mode") == "one") Mode.ONE else Mode.TWO,
            Duration.ofMillis(obj.getLong("defaultSlotDuration")),
            Duration.ofMillis(obj.getLong("notificationTime")),
            Duration.ofMillis(obj.getLong("delayNotificationTime")),
            Duration.ofMillis(obj.getLong("prioritizationTime"))
        )
    }
}
