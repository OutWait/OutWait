package elite.kit.outwait.recyclerviewScreens.managementViewScreen

import elite.kit.outwait.navigation.Navigator
import javax.inject.Inject

class ManagementViewCoordinator @Inject constructor(private val navigator: Navigator) {
    fun navigateToAddDialogFragment() {
        navigator.navigate(ManagementViewFragmentDirections.actionManagementViewFragmentToAddSlotDialogFragment())
    }

    fun navigateToConfigDialog() {
        navigator.navigate(ManagementViewFragmentDirections.actionManagementViewFragmentToConfigDialogFragment2())
    }

}
