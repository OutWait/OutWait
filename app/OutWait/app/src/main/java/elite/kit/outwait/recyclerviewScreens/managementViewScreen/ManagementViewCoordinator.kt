package elite.kit.outwait.recyclerviewScreens.managementViewScreen

import elite.kit.outwait.navigation.Navigator
import elite.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import javax.inject.Inject

class ManagementViewCoordinator @Inject constructor(private val navigator: Navigator) {
    fun navigateToAddDialogFragment() {
        navigator.navigate(ManagementViewFragmentDirections.actionManagmentViewFragmentToAddSlotDialogFragment())
    }

    fun navigateToConfigDialog() {
        navigator.navigate(ManagementViewFragmentDirections.actionManagmentViewFragmentToConfigDialogFragment())
    }

    fun navigateToEditDialogFragment(timeSlot: ClientTimeSlot) {
       // navigator.navigate(ManagmentViewFragmentDirections.actionManagmentViewFragmentToEditTimeSlotDialogFragment(timeSlot))
    }
}
