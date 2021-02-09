package elite.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.json.JSONObject

class JSONAddFixedSlotWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    constructor(slotCode: String, appointmentTime: DateTime) : this(JSONObject()) {

        //TODO Konvertieren wir in Millisekunden oder Sekunden?
        val timeStamp: Long = appointmentTime.millis

        jsonObj.put(SLOT_CODE, slotCode)
        jsonObj.put(APPOINTMENT_TIME, timeStamp)
    }

    /* getter nicht nötig, da wir dieses Objekt nur verschicken, aber
       nie selbst erhalten
     */
}
