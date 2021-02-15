package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.management.ManagementDetails
import edu.kit.outwait.server.management.SlotManagementInformation
import java.time.Duration
import java.util.Date
import org.json.JSONObject

class JSONSlotDataWrapper(obj: JSONObject) : JSONSlotCodeWrapper(obj) {
    constructor() : this(JSONObject())
    fun setSlotApprox(slotApprox: Date) {
        obj.put("approxTime", slotApprox.getTime() / 1000)
    }
    fun setInformation(information: SlotManagementInformation) {
        obj.put("instituteName", information.details.name)
        obj.put("notificationTime", information.notificationTime.getSeconds())
        obj.put("delayNotificationTime", information.delayNotificationTime.getSeconds())
    }
    fun getSlotApprox(): Date {
        return Date(obj.getLong("approxTime") * 1000)
    }
    fun getInformation(): SlotManagementInformation {
        return SlotManagementInformation(
            ManagementDetails(obj.getString("instituteName")),
            Duration.ofSeconds(obj.getLong("notificationTime")),
            Duration.ofSeconds(obj.getLong("delayNotificationTime"))
        )
    }
}
