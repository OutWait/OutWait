package elite.kit.outwait.networkProtocol

import org.json.JSONObject

enum class Event(private val eventString: String,
                 private val wrapperFromJSON: (JSONObject) -> JSONObjectWrapper) {

    LISTEN_SLOT("listenSlot@S", { JSONSlotCodeWrapper(it) }),
    REFRESH_SLOT_APPROX("refreshSlotApprox@S", { JSONSlotCodeWrapper(it) }),

    READY_TO_SERVE("readyToServe@C", { JSONEmptyWrapper(it) }),
    SEND_SLOT_DATA("sendSlotData@C", { JSONSlotDataWrapper(it) }),
    END_SLOT("endSlot@C", { JSONSlotCodeWrapper(it) }),
    DELETE_SLOT("deleteSlot@C", { JSONSlotCodeWrapper(it) }),
    INVALID_CODE("invalidCode@C", { JSONEmptyWrapper(it) }),
    INVALID_REQUEST("invalidRequest@C", { JSONErrorMessageWrapper(it)}),

    MANAGEMENT_LOGIN("managementLogin@S", { JSONLoginWrapper(it) }),
    MANAGEMENT_LOGOUT("managementLogout@S", { JSONEmptyWrapper(it) }),
    START_TRANSACTION("startTransaction@S", { JSONEmptyWrapper(it) }),
    ABORT_TRANSACTION("abortTransaction@S", { JSONEmptyWrapper(it) }),
    SAVE_TRANSACTION("saveTransaction@S", { JSONEmptyWrapper(it) }),
    END_CURRENT_SLOT("endCurrentSlot@S", { JSONEmptyWrapper(it) }),
    CHANGE_MANAGEMENT_SETTINGS("changeManagementSettings@S", { JSONManagementSettingsWrapper(it) }),
    MOVE_SLOT_AFTER_ANOTHER("moveSlotAfterAnother@S", { JSONMoveSlotWrapper(it) }),
    CHANGE_FIXED_SLOT_TIME("changeFixedSlotTime@S", { JSONChangeSlotTimeWrapper(it) }),
    ADD_SPONTANEOUS_SLOT("addSpontaneousSlot@S", { JSONAddSpontaneousSlotWrapper(it) }),
    ADD_FIXED_SLOT("addFixedSlot@S", { JSONAddFixedSlotWrapper(it) }),
    CHANGE_SLOT_DURATION("changeSlotDuration@S", { JSONChangeSlotDurationWrapper(it) }),
    RESET_PASSWORD("resetPassword@S", { JSONResetPasswordWrapper(it) }),

    LOGIN_REQUEST("loginRequest@M", { JSONEmptyWrapper(it) }),
    MANAGEMENT_LOGIN_SUCCESS("managementLoginSuccess@M", { JSONEmptyWrapper(it) }),
    MANAGEMENT_LOGIN_DENIED("managementLoginDenied@M", { JSONEmptyWrapper(it) }),
    TRANSACTION_STARTED("transactionStarted@M", { JSONEmptyWrapper(it) }),
    TRANSACTION_DENIED("transactionDenied@M", { JSONEmptyWrapper(it) }),
    UPDATE_MANAGEMENT_SETTINGS("updateManagementSettings@M", { JSONManagementSettingsWrapper(it) }),
    UPDATE_QUEUE("updateQueue@M", { JSONQueueWrapper(it) });

    fun getEventString(): String {
        return eventString
    }

    fun createWrapper(jsonObj: JSONObject): JSONObjectWrapper {
        return wrapperFromJSON(jsonObj)
    }

}
