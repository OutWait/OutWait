package elite.kit.outwait.clientScreens.editCodeScreen

import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.navigation.Navigator
import javax.inject.Inject

class EditCodeCoordinator @Inject constructor(private val navigator: Navigator) {

    fun navigateToRemainingTimeFragment(){
        navigator.navigate(EditCodeFragmentDirections.actionEditCodeFragmentToRemainingTimeFragment2())

    }

    fun navigateToInstitutLoginFragment(){
        navigator.navigate(EditCodeFragmentDirections.actionEditCodeFragmentToInstitutLoginFragment())
    }
}
