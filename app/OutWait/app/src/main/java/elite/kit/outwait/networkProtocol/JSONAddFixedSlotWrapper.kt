package elite.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

/*
Has no getters, as we only emit the wrapped JSONObject
 */
class JSONAddFixedSlotWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    constructor(duration: Duration, appointmentTime: DateTime) : this(JSONObject()) {

        val timeStampDuration: Long = duration.millis
        val timeStampAppointment: Long = appointmentTime.millis

        jsonObj.put(DURATION, timeStampDuration)
        jsonObj.put(APPOINTMENT_TIME, timeStampAppointment)
    }

}
