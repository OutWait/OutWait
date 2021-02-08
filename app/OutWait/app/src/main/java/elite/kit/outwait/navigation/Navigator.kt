package elite.kit.outwait.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Singleton

@Singleton
class Navigator @Inject constructor() : CoroutineScope by MainScope() {

    private val _navigationActions = MutableSharedFlow<NavController.() -> Unit>()
    val navigationActions: SharedFlow<NavController.() -> Unit> = _navigationActions

    fun navigate(direction: NavDirections) {
        navAction {
            navigate(direction)
        }
    }

    private fun navAction(action: NavController.() -> Unit) {
        launch {
            _navigationActions.emit(action)
        }
    }
}

