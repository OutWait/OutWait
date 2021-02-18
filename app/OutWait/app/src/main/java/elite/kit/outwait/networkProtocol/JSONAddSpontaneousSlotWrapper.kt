package elite.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

/*
Has no getters, as we only emit the wrapped JSONObject
 */
class JSONAddSpontaneousSlotWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    constructor(duration: Duration, timeOfCreation: DateTime) : this(JSONObject()) {
       //TODO JODA Einheiten in Millis aber wir brauchen Sekunden?
        val timeStampDuration: Long = duration.millis
        val timeStampCreation: Long = timeOfCreation.millis

        jsonObj.put(DURATION, timeStampDuration)
        jsonObj.put(TIME_OF_CREATION,timeStampCreation)
    }

}
