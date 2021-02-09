package elite.kit.outwait.networkProtocol

import org.json.JSONObject

class JSONMoveSlotWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    constructor(movedSlot: String, otherSlot: String) : this(JSONObject()) {
        jsonObj.put(MOVED_SLOT, movedSlot)
        jsonObj.put(OTHER_SLOT, otherSlot)
    }

    /*
    Eigentlich brauchen wir diese getter nicht, da wir das Objekt nur versenden, nie erhalten

    fun getMovedSlot(): String {
        return jsonObj.getString(MOVED_SLOT)
    }

    fun getOtherSlot(): String {
        return jsonObj.getString(OTHER_SLOT)
    }

     */
}
