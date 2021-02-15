package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.management.ManagementDetails
import edu.kit.outwait.server.management.SlotManagementInformation
import java.time.Duration
import java.util.Date
import org.json.JSONObject

class JSONSlotDataWrapper(obj: JSONObject) : JSONSlotCodeWrapper(obj) {
    constructor() : this(JSONObject())
    fun setSlotApprox(slotApprox: Date) {}
    fun setInformation(information: SlotManagementInformation) {}
    fun getSlotApprox(): Date { return Date(0) }
    fun getInformation(): SlotManagementInformation {
        return SlotManagementInformation(ManagementDetails(""), Duration.ZERO, Duration.ZERO)
    }
}
