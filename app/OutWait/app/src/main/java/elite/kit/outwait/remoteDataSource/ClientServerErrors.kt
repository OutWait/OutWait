package elite.kit.outwait.remoteDataSource

/**
 * This enumeration represents error messages in the client-server communication,
 * that the client repository (or higher tier) should handle (eg. so the
 * app-user can get notified in an useful way)
 * //TODO Noch was zu den einzelnen Errors sagen?
 */
enum class ClientServerErrors {
    INVALID_SLOT_CODE,
    INVALID_REQUEST,

    /**
     * Errors indicating that the connection could not be established or
     * server did not respond after a max. amount of waiting time or
     * the connection was lost,
     * initComm has to be called again for further communication attempts
     */
    COULD_NOT_CONNECT,
    SERVER_DID_NOT_RESPOND,
    NETWORK_ERROR
}
