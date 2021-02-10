package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.management.ManagementSettings
import edu.kit.outwait.server.management.Mode
import java.time.Duration
import org.json.JSONObject

class JSONManagementSettingsWrapper(obj: JSONObject) : JSONObjectWrapper(obj) {
    constructor() : this(JSONObject())
    fun setSettings(settings: ManagementSettings) {}
    fun getSettings(): ManagementSettings {
        return ManagementSettings(
            Mode.ONE,
            Duration.ZERO,
            Duration.ZERO,
            Duration.ZERO,
            Duration.ZERO
        )}
}
