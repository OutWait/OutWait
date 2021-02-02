package edu.kit.outwait.server.protocol

enum class Event( val tag: String ) {
    MANAGEMENT_LOGIN("managementLogin@S"),
    MANAGEMENT_LOGOUT("managementLogout@S"),
    START_TRANSACTION("startTransaction@S"),
    ABORT_TRANSACTION("abortTransaction@S"),
    SAVE_TRANSACTION("saveTransaction@S"),
    DELETE_SLOT("deleteSlot@S"),
    END_CURRENT_SLOT("endCurrentSlot@S"),
    CHANGE_MANAGEMENT_SETTINGS("changeManagementSettings@S"),
    MOVE_SLOT_AFTER_ANOTHER("moveSlotAfterAnother@S"),
    CHANGE_FIXED_SLOT_TIME("changeFixedSlotTime@S"),
    ADD_SPONTANEOUS_SLOT("addSpontaneousSlot@S"),
    ADD_FIXED_SLOT("addFixedSlot@S"),
    CHANGE_SLOT_DURATION("changeSlotDuration@S"),
    RESET_PASSWORD("resetPassword@S"),
    TRANSACTION_STARTED("transactionStarted@M"),
    TRANSACTION_DENIED("transactionDenied@M"),
    LOGIN_REQUEST("loginRequest@M"),
    MANAGEMENT_LOGIN_SUCCESS("managementLoginSuccess@M"),
    MANAGEMENT_LOGIN_DENIED("managementLoginDenied@M"),
    UPDATE_MANAGEMENT_SETTING("updateManagementSettings@M"),
    UPDATE_QUEUE("updateQueue@M"),
    INVALID_MANAGEMENT_REQUEST("invalidRequest@M"),
    LISTEN_SLOT("listenSLot@S"),
    REFRESH_SLOT_APPROX("refreshSlotApprox@S"),
    READY_TO_SERVE("readyToServe@C"),
    UPDATE_MANAGEMENT_INFORMATION("updateManagementInformation@C"),
    SEND_SLOT_APPROX("sendSlotApprox@C"),
    SLOT_ENDED("endSlot@C"),
    SLOT_DELETED("deleteSlot@C"),
    INVALID_CODE("invalidCode@C"),
    INVALID_CLIENT_REQUEST("invalidRequest@C");

    fun getEventTag(): String { return tag; }
}
