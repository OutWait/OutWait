package edu.kit.outwait.networkProtocol

import org.json.JSONObject

/**
 * These enums represent all the events specified in the server-client-communication protocol
 * with the associated event string and a JSONObjectWrapper for the sent data
 * @see design documentation (section 9 "Kommunikation App-Server for semantic meaning of the events)
 *
 * @property eventString the associated event String transmitted with the data
 * @property wrapperFromJSON the transmitted data for the respective event
 */
enum class Event(private val eventString: String,
                 private val wrapperFromJSON: (JSONObject) -> JSONObjectWrapper) {

    /**
     * events related only to client-server communcation
     */
    LISTEN_SLOT("listenSlot@S", { JSONSlotCodeWrapper(it) }),
    REFRESH_SLOT_APPROX("refreshSlotApprox@S", { JSONSlotCodeWrapper(it) }),
    READY_TO_SERVE_C("readyToServe@C", { JSONEmptyWrapper(it) }),
    SEND_SLOT_DATA_C("sendSlotData@C", { JSONSlotDataWrapper(it) }),
    END_SLOT_C("endSlot@C", { JSONSlotCodeWrapper(it) }),
    DELETE_SLOT_C("deleteSlot@C", { JSONSlotCodeWrapper(it) }),
    INVALID_CODE_C("invalidCode@C", { JSONSlotCodeWrapper(it) }),
    INVALID_REQUEST_C("invalidRequest@C", { JSONErrorMessageWrapper(it) }),


    /**
     * events related to management-server communication
     */
    MANAGEMENT_LOGIN("managementLogin@S", { JSONLoginWrapper(it) }),
    MANAGEMENT_LOGOUT("managementLogout@S", { JSONEmptyWrapper(it) }),
    START_TRANSACTION("startTransaction@S", { JSONEmptyWrapper(it) }),
    ABORT_TRANSACTION("abortTransaction@S", { JSONEmptyWrapper(it) }),
    SAVE_TRANSACTION("saveTransaction@S", { JSONEmptyWrapper(it) }),
    DELETE_SLOT("deleteSlot@S", { JSONSlotCodeWrapper(it) }),
    END_CURRENT_SLOT("endCurrentSlot@S", { JSONEmptyWrapper(it) }),
    CHANGE_MANAGEMENT_SETTINGS("changeManagementSettings@S", { JSONManagementSettingsWrapper(it) }),
    MOVE_SLOT_AFTER_ANOTHER("moveSlotAfterAnother@S", { JSONMoveSlotWrapper(it) }),
    CHANGE_FIXED_SLOT_TIME("changeFixedSlotTime@S", { JSONChangeSlotTimeWrapper(it) }),
    ADD_SPONTANEOUS_SLOT("addSpontaneousSlot@S", { JSONAddSpontaneousSlotWrapper(it) }),
    ADD_FIXED_SLOT("addFixedSlot@S", { JSONAddFixedSlotWrapper(it) }),
    CHANGE_SLOT_DURATION("changeSlotDuration@S", { JSONChangeSlotDurationWrapper(it) }),
    RESET_PASSWORD("resetPassword@S", { JSONResetPasswordWrapper(it) }),

    LOGIN_REQUEST_M("loginRequest@M", { JSONEmptyWrapper(it) }),
    MANAGEMENT_LOGIN_SUCCESS_M("managementLoginSuccess@M", { JSONEmptyWrapper(it) }),
    MANAGEMENT_LOGIN_DENIED_M("managementLoginDenied@M", { JSONEmptyWrapper(it) }),
    TRANSACTION_STARTED_M("transactionStarted@M", { JSONEmptyWrapper(it) }),
    TRANSACTION_DENIED_M("transactionDenied@M", { JSONEmptyWrapper(it) }),
    UPDATE_MANAGEMENT_SETTINGS_M("updateManagementSettings@M", { JSONManagementSettingsWrapper(it) }),
    UPDATE_QUEUE_M("updateQueue@M", { JSONQueueWrapper(it) }),
    INVALID_REQUEST_M("invalidRequest@M", { JSONErrorMessageWrapper(it) }),
    INTERNAL_SERVER_ERROR_M("internalServerError@M", { JSONErrorMessageWrapper(it) }),

    NETWORK_ERROR("NETWORK_ERROR", { JSONEmptyWrapper(it) });

    /**
     * This method returns the event string that is associated with the event
     *
     * @return eventString for the respective event
     */
    fun getEventString(): String {
        return eventString
    }

    /**
     * This method returns, for a given JSONObject, a wrapped JSONObject, using the
     * associated JSONObjectWrapper of the respective event
     *
     * @param jsonObj JSONObject that is to be wrapped
     * @return JSONObjectWrapper of the respective event, containing the given JSONObject
     */
    fun createWrapper(jsonObj: JSONObject): JSONObjectWrapper {
        return wrapperFromJSON(jsonObj)
    }

}
