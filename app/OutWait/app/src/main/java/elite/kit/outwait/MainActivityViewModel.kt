package elite.kit.outwait

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.clientRepository.ClientRepository
import elite.kit.outwait.instituteRepository.InstituteRepository
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private var instituteRepository: InstituteRepository, private var clientRepository: ClientRepository) : ViewModel() {

    fun instituteErrorNotifications() = instituteRepository.getErrorNotifications()
    fun clientErrorNotifications() = clientRepository.getErrorNotifications()

}
