package elite.kit.outwait.networkProtocol

import org.json.JSONObject

enum class ClientEvents(private val eventString: String, private val wrapper: (JSONObject) -> JSONObjectWrapper) {

    //TODO EventENums mit JSONWarpper verbinden und createrWrapper methode

    LISTEN_SLOT("listenSlot@S", { JSONSlotCodeWrapper(it) }),
    REFRESH_SLOT_APPROX("refreshSlotApprox@S", { JSONSlotCodeWrapper(it) }),

    READY_TO_SERVE("readyToServe@C", { JSONEmptyWrapper(it) }),
    SEND_SLOT_DATA("sendSlotData@C", { JSONSlotDataWrapper(it) }),
    END_SLOT("endSlot@C", { JSONSlotCodeWrapper(it) }),
    DELETE_SLOT("deleteSlot@C", { JSONSlotCodeWrapper(it) }),
    INVALID_CODE("invalidCode@C", { JSONEmptyWrapper(it) }),
    INVALID_REQUEST("invalidRequest@C", { JSONInvalidRequestWrapper(it)});

    fun getEventString(): String {
        return eventString
    }

    fun createWrapper(jsonObj: JSONObject): JSONObjectWrapper {
        return wrapper(jsonObj)
    }

}
