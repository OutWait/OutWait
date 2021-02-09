package elite.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.json.JSONObject

class JSONChangeSlotTimeWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    constructor(slotCode: String, newTime: DateTime) : this(JSONObject()) {
        //TODO DateTime konvertieren in Millis oder Sek?
        val timeStampNewTime: Long = newTime.millis

        jsonObj.put(SLOT_CODE, slotCode)
        jsonObj.put(NEW_TIME, timeStampNewTime)
    }

    /*
    Eigentlich brauchen wir diese getter nicht, da wir das Objekt nur versenden, nie erhalten

    fun getSlotCode(): String {
        return jsonObj.getString(SLOT_CODE)
    }

    fun getNewTime(): DateTime {
        TODO("Not yet implemented")
        //TODO UNIX Timestampt JSON String in DateTime konvertieren
    }

     */
}
