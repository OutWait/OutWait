package edu.kit.outwait.server.protocol

import edu.kit.outwait.server.management.ManagementDetails
import edu.kit.outwait.server.management.SlotManagementInformation
import java.time.Duration
import java.util.*
import org.json.JSONObject

/**
 * Json wrapper for slot data.
 *
 * @param obj the json object that should be wrapped.
 * @constructor Creates a new json wrapper from a json object.
 */
class JSONSlotDataWrapper(obj: JSONObject) : JSONSlotCodeWrapper(obj) {
    /**
     * Secondary constructor with no parameter.
     *
     * Use this to create an empty json wrapper, that will be filled with data.
     */
    constructor() : this(JSONObject())

    /**
     * Setter for the approximated time
     *
     * @param slotApprox the approximated time
     */
    fun setSlotApprox(slotApprox: Date) {
        obj.put("approxTime", slotApprox.getTime())
    }

    /**
     * Setter for the slot management information
     *
     * @param information the slot management information
     */
    fun setInformation(information: SlotManagementInformation) {
        obj.put("instituteName", information.details.name)
        obj.put("notificationTime", information.notificationTime.toMillis())
        obj.put("delayNotificationTime", information.delayNotificationTime.toMillis())
    }

    /**
     * Getter for the approximated time
     *
     * @return the approximated time
     */
    fun getSlotApprox(): Date {
        return Date(obj.getLong("approxTime"))
    }

    /**
     * Getter for the slot management information
     *
     * @return the slot management information
     */
    fun getInformation(): SlotManagementInformation {
        return SlotManagementInformation(
            // The email is not part of the protocol, so it's set to ""
            ManagementDetails(obj.getString("instituteName"), ""),
            Duration.ofMillis(obj.getLong("notificationTime")),
            Duration.ofMillis(obj.getLong("delayNotificationTime"))
        )
    }
}
