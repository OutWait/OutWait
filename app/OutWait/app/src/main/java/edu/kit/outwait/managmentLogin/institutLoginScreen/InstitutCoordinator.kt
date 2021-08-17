package edu.kit.outwait.managmentLogin.institutLoginScreen

import android.text.Layout
import edu.kit.outwait.R
import edu.kit.outwait.navigation.Navigator
import javax.inject.Inject

class InstitutCoordinator @Inject constructor(private val navigator: Navigator) {
    fun navigateToManagementView() {
//        navigator.navigate(InstitutLoginFragmentDirections.actionInstitutLoginFragmentToManagmentViewFragment())
    }
    fun navigateToPasswordForgot() {
//        navigator.navigate(InstitutLoginFragmentDirections.actionInstitutLoginFragmentToPasswordForgotFragment())
    }
}
