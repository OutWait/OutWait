package edu.kit.outwait.clientScreens.remainingTimeScreen

import edu.kit.outwait.navigation.Navigator
import javax.inject.Inject

class RemainingTimeCoordinator @Inject constructor(private val navigator: Navigator) {
    fun navigateToForwarderFragment() {
        navigator.navigate(RemainingTimeFragmentDirections.actionRemainingTimeFragmentToForwarderFragment())
    }

}
