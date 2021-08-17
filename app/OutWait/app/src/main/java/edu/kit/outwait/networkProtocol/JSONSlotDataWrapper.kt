package edu.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

/**
 * The JSONObjectWrapper for the data of the "sendSlotData@C" event, that is to be received
 *
 * @constructor
 * Primary constructor takes a given JSONObject and wraps it, using the
 * constructor of the base class
 *
 * @param jsonObj The JSONObject that is to be wrapped (containing the received data of the event)
 */
class JSONSlotDataWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    /**
     * Getter for the slotCode of the received slot data
     *
     * @return slotCode as String, parsed from the received JSONObject
     */
    fun getSlotCode(): String {
        return jsonObj.getString(SLOT_CODE)
    }

    /**
     * Getter for the approximated time of the received slot (data)
     *
     * @return approximated time of the slot as DateTime object
     */
    fun getApproxTime(): DateTime {
        // Creates a DateTime object from a timestamp of type Long
        return DateTime(jsonObj.getLong(APPROX_TIME))
    }

    /**
     * Getter for the institute name of the received slot (data)
     *
     * @return institute name of the slot as String
     */
    fun getInstituteName(): String {
        return jsonObj.getString(INSTITUTE_NAME)
    }

    /**
     * Getter for the configured notification time of the received slot (data)
     *
     * @return notification time of the slot as Duration object
     */
    fun getNotificationTime(): Duration {
        // Creates a Duration object from a timestamp of type Long
        return Duration(jsonObj.getLong(NOTIFICATION_TIME))
    }

    /**
     * Getter for the configured delay notification time of the received slot (data)
     *
     * @return delay notification time of the slot as Duration object
     */
    fun getDelayNotificationTime(): Duration {
        // Creates a Duration object from a timestamp of type Long
        return Duration(jsonObj.getLong(DELAY_NOTIFICATION_TIME))
    }

}
