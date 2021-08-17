package edu.kit.outwait.remoteDataSource

/**
 * This enumeration represents, possibly time-displaced, error messages
 * in the client-server communication, that the management repository
 * (or higher tier) should handle
 */
enum class ManagementServerErrors {

    /**
     * indicates that the transaction start was denied
     */
    TRANSACTION_DENIED,

    /**
     * indicates that the login was denied
     */
    LOGIN_DENIED,

    /**
     * indicates that the server could not respond the request due to an internal
     * issue
     */
    INTERNAL_SERVER_ERROR,

    /**
     * indicates that the server deemed the performed request as invalid
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
