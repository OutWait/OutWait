package edu.kit.outwait.clientRepository

import edu.kit.outwait.R

/**
 * Collection of errors that the client repo can use to inform others
 * (especially the GUI with LiveData) about errors which happen
 *
 * @property message String resource ID where the error message can be found.
 */
enum class ClientErrors(val message: Int) {
    INVALID_SLOT_CODE(R.string.INVALID_SLOT_CODE),
    EXPIRED_SLOT_CODE(R.string.EXPIRED_SLOT_CODE),
    INTERNAL_ERROR(R.string.INTERNAL_ERROR),
    INTERNET_ERROR(R.string.INTERNET_ERROR)
}
