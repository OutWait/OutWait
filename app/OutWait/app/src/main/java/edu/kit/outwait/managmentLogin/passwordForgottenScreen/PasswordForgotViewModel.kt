package edu.kit.outwait.managmentLogin.passwordForgottenScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.kit.outwait.instituteRepository.InstituteRepository
import edu.kit.outwait.managmentLogin.institutLoginScreen.InstitutCoordinator
import org.joda.time.DateTime
import javax.inject.Inject

/***
 * Keeps data from passwordForgotFragment
 */
@HiltViewModel
class PasswordForgotViewModel @Inject constructor(private val repo : InstituteRepository, private val coordinator: InstitutCoordinator) : ViewModel() {

    /**
     * Entered name from management
     */
    val instituteName=MutableLiveData<String>("")

    /**
     * Transmits to reset the password from management
     *
     */
    fun resetPassword(){
        repo.passwordForgotten(instituteName.value!!)
    }
}
