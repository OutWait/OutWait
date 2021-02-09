package elite.kit.outwait.recyclerviewScreens.managmentViewScreen

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.instituteRepository.InstituteRepository
import elite.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
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

    fun navigateToEditDialog(timeSlot: ClientTimeSlot) {
        coordinator.navigateToEditDialogFragment(timeSlot)
    }

    // val slotList:LiveData<List<TimeSlot>>=institutRepository.getAllSlots().asLiveData()


}
