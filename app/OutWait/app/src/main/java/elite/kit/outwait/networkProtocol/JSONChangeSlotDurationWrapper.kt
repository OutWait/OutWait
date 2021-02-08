package elite.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

class JSONChangeSlotDurationWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    constructor(slotCode: String, newDuration: Duration) : this(JSONObject()) {
        jsonObj.put(SLOT_CODE, slotCode)
        //TODO Duration konvertieren und in jsonObj putten
    }

    fun getSlotCode(): String {
        return jsonObj.getString(SLOT_CODE)
    }

    fun getNewDuration(): Duration {
        TODO("Not yet implemented")
        //TODO json UNIX Timestamp in Duration konvertieren
    }
}
