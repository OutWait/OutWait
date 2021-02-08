package elite.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

class JSONSlotApproxWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    constructor(slotCode: String, approxTime: DateTime) : this(JSONObject()) {
        jsonObj.put(SLOT_CODE, slotCode)
        //TODO Joda EInheit in UNIX TImeStamp umwandeln
    }

    fun getSlotCode(): String {
        return jsonObj.getString(SLOT_CODE)
    }

    fun getApproxTime(): DateTime {
        TODO("JSON String in Joda Einheit umwandlen")
    }
}
