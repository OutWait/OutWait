package edu.kit.outwait.protocol

class JSONManagementSettingsWrapper : JSONObjectWrapper {
    constructor() {}
    constructor(object: JSONObject) {}
    fun setSettings(settings: ManagementSettings) {}
    fun getSettings(): ManagementSettings {}
}
