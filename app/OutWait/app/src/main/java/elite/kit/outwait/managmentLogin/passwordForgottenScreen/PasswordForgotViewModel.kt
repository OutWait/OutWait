package elite.kit.outwait.managmentLogin.passwordForgottenScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.instituteRepository.InstituteRepository
import elite.kit.outwait.managmentLogin.institutLoginScreen.InstitutCoordinator
import org.joda.time.DateTime
import javax.inject.Inject

@HiltViewModel
class PasswordForgotViewModel @Inject constructor(private val repo : InstituteRepository, private val coordinator: InstitutCoordinator) : ViewModel() {

    /*
    * - passwordForgotten is void, is there a message neccessary ?
    * */
    val institutName=MutableLiveData<String>()

    fun resetPassword(){
        //TODO Call method of Repository with the para institutName
    }
}
