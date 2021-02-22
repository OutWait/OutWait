package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.management.ManagementSettings
import edu.kit.outwait.server.management.Mode
import java.time.Duration
import org.json.JSONObject

class JSONManagementSettingsWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
    fun setSettings(settings: ManagementSettings) {
        obj.put("defaultSlotDuration", settings.defaultSlotDuration.toMillis())
        obj.put("notificationTime", settings.notificationTime.toMillis())
        obj.put("delayNotificationTime", settings.delayNotificationTime.toMillis())
        obj.put("mode", if (settings.mode == Mode.ONE) "one" else "two")
        obj.put("prioritizationTime", settings.prioritizationTime.toMillis())
    }
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
