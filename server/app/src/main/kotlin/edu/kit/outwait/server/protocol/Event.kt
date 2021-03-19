package edu.kit.outwait.server.protocol

import org.json.JSONObject

/**
 * Enumeration of all possible network protocol events.
 *
 * The events must be used according to the design document.
 *
 * @property tag the type tag that is send over the network to identify the Event. It ends with
 *     '@S', '@M' or '@C' to specify whether the event is designated for the Server, the Management
 *     or the Client respectively.
 * @property wrapper a function to create the dedicated json wrapper from a json string.
 * @constructor Internal constructor for event types.
 */
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
    INVALID_MANAGEMENT_REQUEST("invalidRequest@M", { JSONErrorMessageWrapper(JSONObject(it)) }),
    INTERNAL_SERVER_ERROR("internalServerError@M", { JSONErrorMessageWrapper(JSONObject(it)) }),
    LISTEN_SLOT("listenSlot@S", { JSONSlotCodeWrapper(JSONObject(it)) }),
    REFRESH_SLOT_APPROX("refreshSlotApprox@S", { JSONSlotCodeWrapper(JSONObject(it)) }),
    READY_TO_SERVE("readyToServe@C", { JSONEmptyWrapper(JSONObject(it)) }),
    SEND_SLOT_DATA("sendSlotData@C", { JSONSlotDataWrapper(JSONObject(it)) }),
    SLOT_ENDED("endSlot@C", { JSONSlotCodeWrapper(JSONObject(it)) }),
    SLOT_DELETED("deleteSlot@C", { JSONSlotCodeWrapper(JSONObject(it)) }),
    INVALID_CODE("invalidCode@C", { JSONSlotCodeWrapper(JSONObject(it)) }),
    INVALID_CLIENT_REQUEST("invalidRequest@C", { JSONErrorMessageWrapper(JSONObject(it)) });

    /**
     * Returns the event tag.
     *
     * @return The event tag
     */
    fun getEventTag(): String { return tag }

    /**
     * Constructs the corresponding json wrapper from a json string.
     *
     * @param dat the json string
     * @return the dedicated json wrapper (with the right dynamic type)
     */
    fun createWrapper(dat: String): JSONObjectWrapper { return wrapper(dat) }
}
