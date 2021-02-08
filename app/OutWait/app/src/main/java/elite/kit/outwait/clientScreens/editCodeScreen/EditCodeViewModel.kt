package elite.kit.outwait.clientScreens.editCodeScreen

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.instituteRepository.InstituteRepository
import javax.inject.Inject

@HiltViewModel
class EditCodeViewModel @Inject constructor(private val repo : InstituteRepository, private val coordinator: EditCodeCoordinator) : ViewModel() {

    val clientSlotCode= MutableLiveData<String>()


    fun loginTried(){
        //TODO repo call expected
        coordinator.navigateToRemainingTimeFragment()
    }

    fun startScan(){
        //TODO start scan in fragment
    }

    fun navigateToInstitutLogin(){
        coordinator.navigateToInstitutLoginFragment()
    }
}
