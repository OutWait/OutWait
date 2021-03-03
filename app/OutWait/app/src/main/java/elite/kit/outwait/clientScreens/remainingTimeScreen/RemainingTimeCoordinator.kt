package elite.kit.outwait.clientScreens.remainingTimeScreen

import elite.kit.outwait.navigation.Navigator
import javax.inject.Inject

class RemainingTimeCoordinator @Inject constructor(private val navigator: Navigator) {
    fun navigateToForwarderFragment() {
        navigator.navigate(RemainingTimeFragmentDirections.actionRemainingTimeFragmentToForwarderFragment())
    }

}
