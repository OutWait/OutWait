package elite.kit.outwait.recyclerviewScreens.managmentViewScreen

import elite.kit.outwait.navigation.Navigator
import javax.inject.Inject

class ManagementViewCoordinator @Inject constructor(private val navigator: Navigator) {
    fun navigateToAddDialogFragment() {
        navigator.navigate(ManagmentViewFragmentDirections.actionManagmentViewFragmentToAddSlotDialogFragment())
    }
}
