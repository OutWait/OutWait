package edu.kit.outwait.server.protocol

import org.json.JSONObject

enum class Event(private val tag: String, private val wrapper: (String) -> JSONObjectWrapper) {
    MANAGEMENT_LOGIN("managementLogin@S", { JSONCredentialsWrapper(JSONObject(it)) }),
    MANAGEMENT_LOGOUT("managementLogout@S", { JSONEmptyWrapper(JSONObject(it)) }),
    START_TRANSACTION("startTransaction@S", { JSONEmptyWrapper(JSONObject(it)) }),
    ABORT_TRANSACTION("abortTransaction@S", { JSONEmptyWrapper(JSONObject(it)) }),
    SAVE_TRANSACTION("saveTransaction@S", { JSONEmptyWrapper(JSONObject(it)) }),
    DELETE_SLOT("deleteSlot@S", { JSONSlotCodeWrapper(JSONObject(it)) }),
    END_CURRENT_SLOT("endCurrentSlot@S", { JSONEmptyWrapper(JSONObject(it)) }),
    CHANGE_MANAGEMENT_SETTINGS(
        "changeManagementSettings@S",
        { JSONManagementSettingsWrapper(JSONObject(it)) }
    ),
    MOVE_SLOT_AFTER_ANOTHER("moveSlotAfterAnother@S", { JSONSlotMovementWrapper(JSONObject(it)) }),
    CHANGE_FIXED_SLOT_TIME("changeFixedSlotTime@S", { JSONChangeSlotTimeWrapper(JSONObject(it)) }),
    ADD_SPONTANEOUS_SLOT("addSpontaneousSlot@S", { JSONAddSpontaneousSlotWrapper(JSONObject(it)) }),
    ADD_FIXED_SLOT("addFixedSlot@S", { JSONAddFixedSlotWrapper(JSONObject(it)) }),
    CHANGE_SLOT_DURATION("changeSlotDuration@S", { JSONChangeSlotDurationWrapper(JSONObject(it)) }),
    RESET_PASSWORD("resetPassword@S", { JSONResetPasswordWrapper(JSONObject(it)) }),
    TRANSACTION_STARTED("transactionStarted@M", { JSONEmptyWrapper(JSONObject(it)) }),
    TRANSACTION_DENIED("transactionDenied@M", { JSONEmptyWrapper(JSONObject(it)) }),
    LOGIN_REQUEST("loginRequest@M", { JSONEmptyWrapper(JSONObject(it)) }),
    MANAGEMENT_LOGIN_SUCCESS("managementLoginSuccess@M", { JSONEmptyWrapper(JSONObject(it)) }),
    MANAGEMENT_LOGIN_DENIED("managementLoginDenied@M", { JSONEmptyWrapper(JSONObject(it)) }),
    UPDATE_MANAGEMENT_SETTINGS(
        "updateManagementSettings@M",
        { JSONManagementSettingsWrapper(JSONObject(it)) }
    ),
    UPDATE_QUEUE("updateQueue@M", { JSONQueueWrapper(JSONObject(it)) }),
    INVALID_MANAGEMENT_REQUEST(
        "invalidRequest@M",
        { JSONInvalidRequestMessageWrapper(JSONObject(it)) }
    ),
    LISTEN_SLOT("listenSLot@S", { JSONSlotCodeWrapper(JSONObject(it)) }),
    REFRESH_SLOT_APPROX("refreshSlotApprox@S", { JSONSlotCodeWrapper(JSONObject(it)) }),
    READY_TO_SERVE("readyToServe@C", { JSONEmptyWrapper(JSONObject(it)) }),
    UPDATE_MANAGEMENT_INFORMATION(
        "updateManagementInformation@C",
        { JSONSlotManagementInformationWrapper(JSONObject(it)) }
    ),
    SEND_SLOT_APPROX("sendSlotApprox@C", { JSONSlotApproxWrapper(JSONObject(it)) }),
    SLOT_ENDED("endSlot@C", { JSONSlotCodeWrapper(JSONObject(it)) }),
    SLOT_DELETED("deleteSlot@C", { JSONSlotCodeWrapper(JSONObject(it)) }),
    INVALID_CODE("invalidCode@C", { JSONEmptyWrapper(JSONObject(it)) }),
    INVALID_CLIENT_REQUEST(
        "invalidRequest@C",
        { JSONInvalidRequestMessageWrapper(JSONObject(it)) }
    );

    fun getEventTag(): String { return tag; }
    fun createWrapper(dat: String): JSONObjectWrapper { return wrapper(dat); }
}
