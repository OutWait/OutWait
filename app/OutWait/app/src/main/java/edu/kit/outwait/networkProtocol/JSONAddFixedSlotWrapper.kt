package edu.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

/**
 * The JSONObjectWrapper for the data of the "addFixedSlot@S" event that is to be transmitted
 *
 * @constructor
 * Primary constructor takes a given JSONObject and wraps it, using the
 * constructor of the base class
 *
 * @param jsonObj The JSONObject that is to be wrapped (which will contain the data for this event)
 */
class JSONAddFixedSlotWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    /**
     * Secondary constructor, takes the data that is to be transmitted and stores it in the
     * (previously empty) JSONObject (of the primary constructor)
     * according to the specified (JSON) protocol
     * @see design documentation (section 9 "Kommunikation App-Server)
     *
     * @param duration the requested duration of a fixedSlot as a Duration object
     * @param appointmentTime the requested appointment time of a fixedSlot as a DateTime object
     */
    constructor(duration: Duration, appointmentTime: DateTime) : this(JSONObject()) {

        val timeStampDuration: Long = duration.millis
        val timeStampAppointment: Long = appointmentTime.millis

        jsonObj.put(DURATION, timeStampDuration)
        jsonObj.put(APPOINTMENT_TIME, timeStampAppointment)
    }

}
