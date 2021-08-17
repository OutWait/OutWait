package edu.kit.outwait.remoteDataSource

/**
 * This enumeration represents possibly time-displaced error messages
 * in the client-server communication, that the client repository
 * (or higher tier) should handle
 */
enum class ClientServerErrors {
    /**
     * indicates that the entered slot code was invalid
     */
    INVALID_SLOT_CODE,

    /**
     * indicates that the received slot code is already expired
     *
     */
    EXPIRED_SLOT_CODE,

    /**
     * indicated that the server deemed the performed request as invalid
     */
    INVALID_REQUEST,

    /**
     * indicates that the connection to the server could not be established,
     * the repository has to initialize the communication again manually
     */
    COULD_NOT_CONNECT,

    /**
     * indicates that the server did not respond until time out,
     * the repository has to initialize the communication again manually
     */
    SERVER_DID_NOT_RESPOND,

    /**
     * indicates that the connection was lost (eg. loss of signal), resulting
     * in the end of the current communication session,
     * the repository has to initialize the communication again manually
     */
    NETWORK_ERROR
}
