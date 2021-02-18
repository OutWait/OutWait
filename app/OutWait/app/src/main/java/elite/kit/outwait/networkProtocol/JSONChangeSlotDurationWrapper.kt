package elite.kit.outwait.networkProtocol

import org.joda.time.Duration
import org.json.JSONObject

/*
Has no getters, as we only emit the wrapped JSONObject
 */
class JSONChangeSlotDurationWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    constructor(slotCode: String, newDuration: Duration) : this(JSONObject()) {
        //TODO Duration konvertieren in Millis oder Sek?
        val timeStampDuration: Long = newDuration.millis

        jsonObj.put(SLOT_CODE, slotCode)
        jsonObj.put(NEW_DURATION, timeStampDuration)
    }

}
