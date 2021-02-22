package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.management.ManagementDetails
import edu.kit.outwait.server.management.SlotManagementInformation
import java.time.Duration
import java.util.*
import org.json.JSONObject

class JSONSlotDataWrapper(obj: JSONObject) : JSONSlotCodeWrapper(obj) {
    constructor() : this(JSONObject())
    fun setSlotApprox(slotApprox: Date) {
        obj.put("approxTime", slotApprox.getTime())
    }
    fun setInformation(information: SlotManagementInformation) {
        obj.put("instituteName", information.details.name)
        obj.put("notificationTime", information.notificationTime.toMillis())
        obj.put("delayNotificationTime", information.delayNotificationTime.toMillis())
    }
    fun getSlotApprox(): Date {
        return Date(obj.getLong("approxTime"))
    }
    fun getInformation(): SlotManagementInformation {
        return SlotManagementInformation(
            ManagementDetails(obj.getString("instituteName")),
            Duration.ofMillis(obj.getLong("notificationTime")),
            Duration.ofMillis(obj.getLong("delayNotificationTime"))
        )
    }
}
