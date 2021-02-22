package elite.kit.outwait.recyclerviewScreens.configurationsScreen

import elite.kit.outwait.navigation.Navigator
import javax.inject.Inject

class ConfigCoordinator @Inject constructor(private val navigator: Navigator) {
    fun navigateToForwarderFragment() {
        navigator.navigate(ConfigDialogFragmentDirections.actionConfigDialogFragment2ToForwarderFragment())
    }


}
