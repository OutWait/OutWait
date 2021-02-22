package elite.kit.outwait.recyclerviewScreens.managmentViewScreen

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.instituteRepository.InstituteRepository
import elite.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import javax.inject.Inject

@HiltViewModel
class ManagmentViewViewModel @Inject constructor(
     val repo: InstituteRepository,
    private val coordinator: ManagementViewCoordinator,
) : ViewModel() {

    val slotQueue= MediatorLiveData<List<TimeSlot>>().apply {
        addSource(repo.getObservableTimeSlotList()) {
            value = it
        }
    }

    /*
    * - zuerst gebewgter slot dann der feste
    * - both delete and endCurrent
    * */

    // val slotList:LiveData<List<TimeSlot>>=institutRepository.getAllSlots().asLiveData()

    fun navigateToAddSlotDialog() {
        coordinator.navigateToAddDialogFragment()
    }

    fun navigateToConfigDialog() {
        coordinator.navigateToConfigDialog()
    }

    fun notifyDeleteSlot(slot:ClientTimeSlot){
        repo.deleteSlot(slot.slotCode)
    }

    fun notifyEndCurrentSlot(){
        repo.endCurrentSlot()
    }



}
