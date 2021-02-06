package elite.kit.outwait.clientScreens.editCodeScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.instituteRepository.InstituteRepository
import elite.kit.outwait.managmentLogin.institutLoginScreen.InstitutCoordinator
import org.joda.time.DateTime
import javax.inject.Inject

@HiltViewModel
class EditCodeViewModel @Inject constructor(private val repo : InstituteRepository, private val coordinator: EditCodeCoordinator) : ViewModel() {

    private lateinit var _clientSlotCode: MutableLiveData<String>
    val clientSlotCode: LiveData<String>
        get() {return _clientSlotCode}

    fun loginTried(){
        coordinator.navigateToRemainingTimeFragment()
    }

    fun startScan(){
        //TODO start scan in fragment
    }

    fun navigateToInstitutLogin(){
        coordinator.navigateToInstitutLoginFragment()
    }
}
