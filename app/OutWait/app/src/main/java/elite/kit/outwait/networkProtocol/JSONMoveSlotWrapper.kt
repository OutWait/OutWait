package elite.kit.outwait.networkProtocol

import org.json.JSONObject

/*
Has no getters, as we only emit the wrapped JSONObject
 */
class JSONMoveSlotWrapper(jsonObj: JSONObject) : JSONObjectWrapper(jsonObj) {

    constructor(movedSlot: String, otherSlot: String) : this(JSONObject()) {
        jsonObj.put(MOVED_SLOT, movedSlot)
        jsonObj.put(OTHER_SLOT, otherSlot)
    }

}
