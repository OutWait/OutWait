package elite.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

/*
Has no getters, as we only emit the wrapped JSONObject
 */
class JSONAddFixedSlotWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    constructor(duration: Duration, appointmentTime: DateTime) : this(JSONObject()) {

        val timeStamp: Long = appointmentTime.millis

        jsonObj.put(DURATION, duration)
        jsonObj.put(APPOINTMENT_TIME, timeStamp)
    }

}
