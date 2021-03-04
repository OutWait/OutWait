package elite.kit.outwait.instituteRepository

import elite.kit.outwait.R

enum class InstituteErrors(val message: Int) {
    TRANSACTION_DENIED(R.string.TRANSACTION_DENIED),
    LOGIN_DENIED(R.string.LOGIN_DENIED),
    NOT_IN_TRANSACTION(R.string.NOT_IN_TRANSACTION),
    NETWORK_ERROR(R.string.NETWORK_ERROR)
}
