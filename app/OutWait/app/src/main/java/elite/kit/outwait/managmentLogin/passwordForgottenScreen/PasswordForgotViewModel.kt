package elite.kit.outwait.managmentLogin.passwordForgottenScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.instituteRepository.InstituteRepository
import elite.kit.outwait.managmentLogin.institutLoginScreen.InstitutCoordinator
import javax.inject.Inject

@HiltViewModel
class PasswordForgotViewModel @Inject constructor(private val repo : InstituteRepository, private val coordinator: InstitutCoordinator) : ViewModel() {
    var institutName:String

    init {
        institutName=""
    }

    fun resetPassword(){
        //TODO Call method of Repository with the para institutName
        Log.i("send","$institutName to reset")
    }
}
