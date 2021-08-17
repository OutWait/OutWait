package edu.kit.outwait.recyclerviewScreens.managementViewScreen

import edu.kit.outwait.navigation.Navigator
import javax.inject.Inject

/**
 * Maintains all navigations from managementView
 *
 * @property navigator Executes navigation
 */
class ManagementViewCoordinator @Inject constructor(private val navigator: Navigator) {
    /**
     * Navigates to addDialogFragment
     *
     */
    fun navigateToAddDialogFragment() {
        navigator.navigate(ManagementViewFragmentDirections.actionManagementViewFragmentToAddSlotDialogFragment())
    }

    /**
     * Navigates to configDialog
     *
     */
    fun navigateToConfigDialog() {
        navigator.navigate(ManagementViewFragmentDirections.actionManagementViewFragmentToConfigDialogFragment2())
    }

}
