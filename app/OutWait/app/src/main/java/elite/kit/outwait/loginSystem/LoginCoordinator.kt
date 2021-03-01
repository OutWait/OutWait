package elite.kit.outwait.loginSystem

import elite.kit.outwait.navigation.Navigator
import javax.inject.Inject

class LoginCoordinator @Inject constructor(private val navigator: Navigator) {
    fun navigateToRemainingTimeFragment() {
        navigator.navigate(ForwarderFragmentDirections.actionForwarderFragmentToRemainingTimeFragment())

    }

    fun navigateToManagementViewFragment() {
        navigator.navigate(ForwarderFragmentDirections.actionForwarderFragmentToManagmentViewFragment())
    }

    fun navigateToLoginFragment() {
        navigator.navigate(ForwarderFragmentDirections.actionForwarderFragmentToLoginFragment())
    }

    fun navigateToPasswordForgottenFragment() {
navigator.navigate(LoginFragmentDirections.actionLoginFragmentToPasswordForgotFragment())
    }
}
