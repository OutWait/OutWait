package elite.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

class JSONAddSpontaneousSlotWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    constructor(duration: Duration, timeOfCreation: DateTime) : this(JSONObject()) {
        TODO("JODA Einheiten in UNIX für JSON konvertieren")
    }
}
