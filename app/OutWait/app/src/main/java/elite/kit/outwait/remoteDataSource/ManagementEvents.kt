package elite.kit.outwait.remoteDataSource

enum class ManagementEvents(eventString: String) {


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

    LOGIN_REQUEST("loginRequest@M"),
    MANAGEMENT_LOGIN_SUCCESS("managementLoginSuccess@M"),
    MANAGEMENT_LOGIN_DENIED("managementLoginDenied@M"),
    TRANSACTION_STARTED("transactionStarted@M"),
    TRANSACTION_DENIED("transactionDenied@M"),
    UPDATE_MANAGEMENT_SETTINGS("updateManagementSettings@M"),
    UPDATE_QUEUE("updateQueue@M"),
    INVALID_REQUEST("invalidRequest@M")

}
