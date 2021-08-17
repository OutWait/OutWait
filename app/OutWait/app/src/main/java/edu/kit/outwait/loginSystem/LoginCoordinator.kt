package edu.kit.outwait.loginSystem

import edu.kit.outwait.navigation.Navigator
import javax.inject.Inject

/**
 * Containes all possible directions of the login screen
 *
 * @property navigator Instance which peforms the navigation
 */
class LoginCoordinator @Inject constructor(private val navigator: Navigator) {
    /**
     * Navigates to RemainingTimeFragment
     *
     */
    fun navigateToRemainingTimeFragment() {
        navigator.navigate(ForwarderFragmentDirections.actionForwarderFragmentToRemainingTimeFragment())
    }
    /**
     * Navigates to ManagementViewFragment
     *
     */
    fun navigateToManagementViewFragment() {
        navigator.navigate(ForwarderFragmentDirections.actionForwarderFragmentToManagementViewFragment())
    }
    /**
     * Navigates to LoginFragment
     *
     */
    fun navigateToLoginFragment() {
        navigator.navigate(ForwarderFragmentDirections.actionForwarderFragmentToLoginFragment())
    }
    /**
     * Navigates to PasswordForgottenFragment
     *
     */
    fun navigateToPasswordForgottenFragment() {
        navigator.navigate(LoginFragmentDirections.actionLoginFragmentToPasswordForgotFragment())
    }
}
