package elite.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.json.JSONObject

/*
Has no getters, as we only emit the wrapped JSONObject
 */
class JSONChangeSlotTimeWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    constructor(slotCode: String, newTime: DateTime) : this(JSONObject()) {
        //TODO DateTime konvertieren in Millis, aber Sekunden parsen!!!
        val timeStampNewTime: Long = newTime.millis

        jsonObj.put(SLOT_CODE, slotCode)
        jsonObj.put(NEW_TIME, timeStampNewTime)
    }

}
