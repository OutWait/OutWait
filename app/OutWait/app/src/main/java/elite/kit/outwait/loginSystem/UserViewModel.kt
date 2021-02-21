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
class UserViewModel @Inject constructor(private val repoClient : ClientRepository, private val repoInstitute: InstituteRepository): ViewModel() {

    val clientSlotCode= MutableLiveData<String>()
    val instituteName= MutableLiveData<String>()
    val institutePassword= MutableLiveData<String>()

    val loginResponse= MediatorLiveData<List<Any>>().apply {
        addSource(repoClient.getActiveSlots()){
            value=it
        }
    }


     var users: LiveData<List<ClientInfo>> = Transformations.switchMap(loginResponse){
         repoClient.getActiveSlots()
     }

    fun signInUser(){
        loginResponse.addSource(repoClient.getActiveSlots()){
            loginResponse.value=it
        }

        /*loginResponse.addSource(LIVEDATA LOGIN INSTITUTE){
            loginResponse.value=it
        }*/

    }


    fun enterSlotCode(code: String?){
        Log.d("enterSCode::EditCodeVM", "reached")
        viewModelScope.launch {
            repoClient.newCodeEntered(code)
        }
    }

    fun login(){
        repoInstitute.login(instituteName.value!!,institutePassword.value!!)
    }


}
