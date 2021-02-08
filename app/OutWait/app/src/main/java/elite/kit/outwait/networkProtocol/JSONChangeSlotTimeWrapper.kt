package elite.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.json.JSONObject

class JSONChangeSlotTimeWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    constructor(slotCode: String, newTime: DateTime) : this(JSONObject()) {
        jsonObj.put("slotCode", slotCode)
        //TODO DateTime konvertieren und in jsonObj putten
    }

    fun getSlotCode(): String {
        return jsonObj.getString(SLOT_CODE)
    }

    fun getNewTime(): DateTime {
        TODO("Not yet implemented")
        //TODO UNIX Timestampt JSON String in DateTime konvertieren
    }
}
