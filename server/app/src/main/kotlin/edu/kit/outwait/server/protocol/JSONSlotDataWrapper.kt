package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.management.ManagementDetails
import edu.kit.outwait.server.management.SlotManagementInformation
import org.json.JSONObject
import java.time.Duration
import java.util.*

class JSONSlotDataWrapper (obj: JSONObject) : JSONSlotCodeWrapper(obj){
    constructor() : this(JSONObject())
    fun setSlotApprox(slotApprox: Date) {}
    fun getSlotApprox(): Date { return Date(0) }
    fun setInformation(information: SlotManagementInformation) {}
    fun getInformation(): SlotManagementInformation {
        return SlotManagementInformation(ManagementDetails(""), Duration.ZERO, Duration.ZERO)
    }
}
