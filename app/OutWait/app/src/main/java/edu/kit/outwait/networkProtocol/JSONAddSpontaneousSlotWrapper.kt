package edu.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

/**
 * The JSONObjectWrapper for the data of the "addSpontaneousSlot@S" event that is to be transmitted
 *
 * @constructor
 * Primary constructor takes a given JSONObject and wraps it, using the
 * constructor of the base class
 *
 * @param jsonObj The JSONObject that is to be wrapped (which will contain the data for this event)
 */
class JSONAddSpontaneousSlotWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    /**
     * Secondary constructor, takes the data that is to be transmitted and stores it in the
     * (previously empty) JSONObject (of the primary constructor)
     * according to the specified (JSON) protocol
     * @see design documentation (section 9 "Kommunikation App-Server)
     *
     * @param duration as a Duration object, the requested duration of a spontaneousSlot
     * @param timeOfCreation timestamp as DateTime object, representing the instant of the
     * requested slot allocation
     */
    constructor(duration: Duration, timeOfCreation: DateTime) : this(JSONObject()) {

        val timeStampDuration: Long = duration.millis
        val timeStampCreation: Long = timeOfCreation.millis

        jsonObj.put(DURATION, timeStampDuration)
        jsonObj.put(TIME_OF_CREATION, timeStampCreation)
    }

}
