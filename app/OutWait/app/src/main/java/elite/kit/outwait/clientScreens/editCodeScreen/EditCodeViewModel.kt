package elite.kit.outwait.clientScreens.editCodeScreen

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.clientDatabase.ClientInfo
import elite.kit.outwait.clientRepository.ClientRepository
import elite.kit.outwait.instituteRepository.InstituteRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditCodeViewModel @Inject constructor(private val repo : ClientRepository, private val coordinator: EditCodeCoordinator) : ViewModel() {

    val clientSlotCode= MutableLiveData<String>()
    val clientSlotCodes= MutableLiveData<String>()

    /*val users: LiveData<List<ClientInfo>> = Transformations.switchMap(clientSlotCode){
        repo.getActiveSlots()
    }*/

    val loginResponse= MediatorLiveData<List<Any>>().apply {
        addSource(repo.getActiveSlots()){
            value=it
        }
    }

    init{
       /* repo.getActiveSlots().observeForever {
            if (it.isNotEmpty()){
                coordinator.navigateToRemainingTimeFragment()
            }
        }*/
    }
    fun enterSlotCode(code: String?){
        Log.d("enterSCode::EditCodeVM", "reached")
        viewModelScope.launch {
            repo.newCodeEntered(code)
        }
    }

    fun navigateToInstitutLogin(){
        coordinator.navigateToInstitutLoginFragment()
    }

    fun navigateToRemainingTimeFragment(){
        coordinator.navigateToRemainingTimeFragment()
    }
}
