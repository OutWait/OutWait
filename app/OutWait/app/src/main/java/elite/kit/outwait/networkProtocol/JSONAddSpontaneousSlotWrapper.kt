package elite.kit.outwait.networkProtocol

import org.joda.time.Duration
import org.json.JSONObject

class JSONAddSpontaneousSlotWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    constructor(slotCode: String, newDuration: Duration) : this(JSONObject()) {

    }
}
