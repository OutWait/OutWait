package elite.kit.outwait.recyclerviewScreens.managementViewScreen

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




    var isInTransaction= MediatorLiveData<Boolean>().apply {
        addSource(repo.isInTransaction()){it ->

            }

    }


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

    fun moveSlotAfterAnother(movedSlot: String, otherSlot: String) {
        repo.moveSlotAfterAnother(movedSlot,otherSlot)
    }

    fun deleteSlot(slotCode:String){
        repo.deleteSlot(slotCode)
    }
    fun endCurrendSlot(){
        repo.endCurrentSlot()
    }

    fun saveTransaction() {
        repo.saveTransaction()
    }

    fun abortTransaction() {
        repo.abortTransaction()
    }


}
