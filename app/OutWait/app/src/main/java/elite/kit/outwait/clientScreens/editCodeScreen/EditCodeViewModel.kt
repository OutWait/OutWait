package elite.kit.outwait.clientScreens.editCodeScreen

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.clientRepository.ClientRepository
import elite.kit.outwait.instituteRepository.InstituteRepository
import javax.inject.Inject

@HiltViewModel
class EditCodeViewModel @Inject constructor(private val repo : ClientRepository, private val coordinator: EditCodeCoordinator) : ViewModel() {

    /*
    * - getActiveSlots is for login system oberservable
    * */

    val clientSlotCode= MutableLiveData<String>()


    fun enterSlotCode(code: String){
        //TODO repo call expected
        Log.d("enterSCode::EditCodeVM", "reached")
        repo.newCodeEntered(code)
        coordinator.navigateToRemainingTimeFragment()
    }

    fun startScan(){
        //TODO start scan in fragment
    }

    fun navigateToInstitutLogin(){
        coordinator.navigateToInstitutLoginFragment()
    }
}
