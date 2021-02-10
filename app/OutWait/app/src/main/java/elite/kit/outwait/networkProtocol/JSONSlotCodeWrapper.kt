package elite.kit.outwait.networkProtocol

import org.json.JSONObject

class JSONSlotCodeWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    constructor(slotCode: String) : this(JSONObject()) {
        jsonObj.put(SLOT_CODE, slotCode)
    }

    fun getSlotCode() {
        jsonObj.getString(SLOT_CODE)
    }
}
