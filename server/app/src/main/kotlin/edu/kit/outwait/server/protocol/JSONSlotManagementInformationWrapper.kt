package edu.kit.outwait.server.protocol

import java.time.Duration

import org.json.JSONObject

import edu.kit.outwait.server.management.SlotManagementInformation
import edu.kit.outwait.server.management.ManagementDetails

class JSONSlotManagementInformationWrapper : JSONSlotCodeWrapper {
    constructor() {}
    constructor(obj: JSONObject) {}
    fun setinformation(information: SlotManagementInformation) {}
    fun getInformation(): SlotManagementInformation { return SlotManagementInformation(ManagementDetails(""), Duration.ZERO, Duration.ZERO)}
}
