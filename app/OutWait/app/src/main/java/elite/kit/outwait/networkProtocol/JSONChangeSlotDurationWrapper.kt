package elite.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

class JSONChangeSlotDurationWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    constructor(slotCode: String, newDuration: Duration) : this(JSONObject()) {
        //TODO Duration konvertieren in Millis oder Sek?
        val timeStampDuration: Long = newDuration.millis

        jsonObj.put(SLOT_CODE, slotCode)
        jsonObj.put(NEW_DURATION, timeStampDuration)
    }

    /*
    TODO Eigentlich brauchen wir diese getter nicht, da wir das Objekt nur versenden, nie erhalten

    fun getSlotCode(): String {
        return jsonObj.getString(SLOT_CODE)
    }

    fun getNewDuration(): Duration {
        //TODO json UNIX Timestamp in Duration konvertieren
    }

     */
}
