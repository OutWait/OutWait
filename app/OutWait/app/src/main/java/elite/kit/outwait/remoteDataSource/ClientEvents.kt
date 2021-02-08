package elite.kit.outwait.remoteDataSource

enum class ClientEvents(private val eventString: String) {

    LISTEN_SLOT("listenSlot@S"),
    REFRESH_SLOT_APPROX("refreshSlotApprox@S"),

    READY_TO_SERVE("readyToServe@C"),
    UPDATE_MANAGEMENT_INFORMATION("updateManagementInformation@C"),
    SEND_SLOT_APPROX("sendSlotApprox@C"),
    END_SLOT("endSlot@C"),
    DELETE_SLOT("deleteSlot@C"),
    INVALID_CODE("invalidCode@C"),
    INVALID_REQUEST("invalidRequest@C");

    fun getEventString(): String {
        return eventString
    }

}
