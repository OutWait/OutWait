package elite.kit.outwait.loginSystem

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.clientDatabase.ClientInfo
import elite.kit.outwait.clientRepository.ClientRepository
import elite.kit.outwait.instituteRepository.InstituteRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val repoClient : ClientRepository, private val repoInstitute: InstituteRepository, private val coordinator:LoginCoordinator): ViewModel() {



    val clientSlotCode= MutableLiveData<String>("")
    val instituteName= MutableLiveData<String>("")
    val institutePassword= MutableLiveData<String>("")

    val loginResponse= MediatorLiveData<List<Any>>().apply {
        addSource(repoClient.getActiveSlots()){
            value=it
        }

        addSource(repoInstitute.isLoggedIn()){
            value= listOf(it)
        }
    }

    fun enterSlotCode(){
        viewModelScope.launch {
            repoClient.newCodeEntered(clientSlotCode.value)
        }
    }

    fun login(){
        Log.i("password","$institutePassword")
     repoInstitute.login(instituteName.value!!, institutePassword.value!!)

    }

    fun navigateToRemainingTimeFragment() {
        coordinator.navigateToRemainingTimeFragment()
    }

    fun navigateToManagementViewFragment() {
        coordinator.navigateToManagementViewFragment()
    }

    fun navigateToLoginFragment() {
        coordinator.navigateToLoginFragment()
    }

    fun navigateToPasswordForgottenFragment() {
        coordinator.navigateToPasswordForgottenFragment()
    }


}
