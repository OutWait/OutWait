package elite.kit.outwait.networkProtocol

import org.joda.time.DateTime
import org.json.JSONObject

class JSONAddFixedSlotWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj)  {

    constructor(slotCode: String, appointmentTime: DateTime) : this(JSONObject()) {
        jsonObj.put(SLOT_CODE, slotCode)
        //TODO
    }
}
