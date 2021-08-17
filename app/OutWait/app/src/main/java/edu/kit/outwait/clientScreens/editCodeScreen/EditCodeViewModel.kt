package edu.kit.outwait.clientScreens.editCodeScreen

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.kit.outwait.clientDatabase.ClientInfo
import edu.kit.outwait.clientRepository.ClientRepository
import edu.kit.outwait.instituteRepository.InstituteRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditCodeViewModel @Inject constructor(private val repo : ClientRepository, private val coordinator: EditCodeCoordinator) : ViewModel() {

    val clientSlotCode= MutableLiveData<String>()

    /*val users: LiveData<List<ClientInfo>> = Transformations.switchMap(clientSlotCode){
        repo.getActiveSlots()
    }*/

    val loginResponse= MediatorLiveData<List<Any>>().apply {
        addSource(repo.getActiveSlots()){
            value=it
        }
    }

    init {

    }

   /* init{
        repo.getActiveSlots().observeForever {
            if (it.isNotEmpty()){
                //test to see whether object is created
                Log.i("object","${it.component1().slotCode}")
                coordinator.navigateToRemainingTimeFragment()
            }
            Log.i("object","${it.isEmpty()}")
        }
    }*/

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
