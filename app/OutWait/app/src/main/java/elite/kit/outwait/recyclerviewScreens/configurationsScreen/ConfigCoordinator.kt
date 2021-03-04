package elite.kit.outwait.recyclerviewScreens.configurationsScreen

import elite.kit.outwait.navigation.Navigator
import javax.inject.Inject

/**
 * Maintains all possible navigations from the configuration screen
 *
 * @property navigator Executer of navigation
 */
class ConfigCoordinator @Inject constructor(private val navigator: Navigator) {
    /**
     * Navigate back to base fragment to check who is logged
     *
     */
    fun navigateToForwarderFragment() {
        navigator.navigate(ConfigDialogFragmentDirections.actionConfigDialogFragment2ToForwarderFragment())
    }


}
