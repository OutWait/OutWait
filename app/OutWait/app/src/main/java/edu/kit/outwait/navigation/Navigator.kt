package edu.kit.outwait.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Singleton

/**
 * Executes to navigate using navGraph from one fragment to other one
 *
 */
@Singleton
class Navigator @Inject constructor() : CoroutineScope by MainScope() {

    private val _navigationActions = MutableSharedFlow<NavController.() -> Unit>()

    /**
     * Containes navController
     */
    val navigationActions: SharedFlow<NavController.() -> Unit> = _navigationActions

    /**
     * Starts navigation
     *
     * @param direction Path of navGraph
     */
    fun navigate(direction: NavDirections) {
        navAction {
            navigate(direction)
        }
    }

    /**
     * Executes navigation with guarantee only one time
     *
     * @param action Emits a value to this shared flow
     */
    private fun navAction(action: NavController.() -> Unit) {

        launch {
            _navigationActions.emit(action)
        }
    }
}


