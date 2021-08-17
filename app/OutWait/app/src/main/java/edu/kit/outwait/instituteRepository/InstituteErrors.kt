package edu.kit.outwait.instituteRepository

import edu.kit.outwait.R

/**
 * Collection of errors that the institute repo can use to inform others
 * (especially the GUI with LiveData) about errors which happen.
 * Errors are always used to notify when methods (e.g. for queue manipulations)
 * do not succeed.
 *
 * @property message String resource ID where the error message can be found.
 */
enum class InstituteErrors(val message: Int) {
    TRANSACTION_DENIED(R.string.TRANSACTION_DENIED),
    LOGIN_DENIED(R.string.LOGIN_DENIED),
    NOT_IN_TRANSACTION(R.string.NOT_IN_TRANSACTION),
    NETWORK_ERROR(R.string.NETWORK_ERROR),
    COMMUNICATION_ERROR(R.string.COMMUNICATION_ERROR),
    SERVER_ERROR(R.string.SERVER_ERROR),
    INVALID_REQUEST(R.string.INVALID_REQUEST)
}
