package elite.kit.outwait.networkProtocol

import elite.kit.outwait.customDataTypes.ReceivedList
import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

class JSONQueueWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    /* Secondary Constructor nicht nötig, da wir dieses Objekt nur erhalten,
       aber nie selbst verschicken
    */

    fun getQueue(): ReceivedList {
        //val currentSlotStartedTime: DateTime = DateTime(jsonObj.getLong())
        TODO("TO implement params auslesen und Received List erzeugen")
    }
}
