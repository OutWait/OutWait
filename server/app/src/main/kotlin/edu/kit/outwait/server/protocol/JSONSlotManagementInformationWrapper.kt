package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.management.ManagementDetails
import edu.kit.outwait.server.management.SlotManagementInformation
import java.time.Duration
import org.json.JSONObject

class JSONSlotManagementInformationWrapper(obj: JSONObject) : JSONSlotCodeWrapper(obj) {
    constructor() : this(JSONObject())
    fun setInformation(information: SlotManagementInformation) {}
    fun getInformation(): SlotManagementInformation {
        return SlotManagementInformation(ManagementDetails(""), Duration.ZERO, Duration.ZERO)}
}
