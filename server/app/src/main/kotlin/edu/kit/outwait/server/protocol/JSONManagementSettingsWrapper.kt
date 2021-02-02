package edu.kit.outwait.server.protocol

import java.time.Duration

import org.json.JSONObject

import edu.kit.outwait.server.management.ManagementSettings
import edu.kit.outwait.server.management.Mode

class JSONManagementSettingsWrapper : JSONObjectWrapper {
    constructor() {}
    constructor(obj: JSONObject) {}
    fun setSettings(settings: ManagementSettings) {}
    fun getSettings(): ManagementSettings { return ManagementSettings( Mode.ONE, Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO )}
}
