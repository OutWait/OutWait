package elite.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

class JSONAddSpontaneousSlotWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    constructor(duration: Duration, timeOfCreation: DateTime) : this(JSONObject()) {
       //TODO JODA Einheiten in Millis oder Sek konvertieren?
        val timeStampDuration: Long = duration.millis
        val timeStampCreation: Long = timeOfCreation.millis

        jsonObj.put(DURATION, timeStampDuration)
        jsonObj.put(TIME_OF_CREATION,timeStampCreation)
    }

    /* getter nicht nötig, da wir dieses Objekt nur verschicken, aber
       nie selbst erhalten
     */
}
