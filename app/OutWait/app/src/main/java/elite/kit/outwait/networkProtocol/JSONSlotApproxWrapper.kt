package elite.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

class JSONSlotApproxWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    /*
    TODO EIgentlich brauchen wir den Konstruktor nicht, da wir dieses Objekt nur erhalten, nie verschicken


    constructor(slotCode: String, approxTime: DateTime) : this(JSONObject()) {

        val timeStampApproxTime: Long = approxTime.millis

        jsonObj.put(SLOT_CODE, slotCode)
        jsonObj.put(APPROX_TIME, timeStampApproxTime)
    }

     */

    fun getSlotCode(): String {
        return jsonObj.getString(SLOT_CODE)
    }

    fun getApproxTime(): DateTime {

        // Creates a DateTime object from a timestamp of type Long
        return DateTime(jsonObj.getLong(APPROX_TIME))
    }
}
