package elite.kit.outwait.recyclerviewScreens.managmentViewScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.instituteRepository.InstituteRepository
import elite.kit.outwait.managmentLogin.institutLoginScreen.InstitutCoordinator
import elite.kit.outwait.waitingQueue.timeSlotModel.Pause
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import javax.inject.Inject

@HiltViewModel
class ManagmentViewViewModel @Inject constructor(private val repo : InstituteRepository, private val coordinator: ManagementViewCoordinator)
    : ViewModel() {


    fun navigateToAddSlotDialog() {
        coordinator.navigateToAddDialogFragment()
    }

    fun navigateToConfigDialog() {
        coordinator.navigateToConfigDialog()
    }
    // val slotList:LiveData<List<TimeSlot>>=institutRepository.getAllSlots().asLiveData()


}
