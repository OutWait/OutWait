package elite.kit.outwait.recyclerviewScreens.managementViewScreen

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.instituteRepository.InstituteRepository
import elite.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import javax.inject.Inject

@HiltViewModel
class ManagementViewViewModel @Inject constructor(
     val repo: InstituteRepository,
    private val coordinator: ManagementViewCoordinator,
) : ViewModel() {

    val slotQueue= MediatorLiveData<List<TimeSlot>>().apply {
        addSource(repo.getObservableTimeSlotList()) {
            value = it
        }
    }




    var isInTransaction= repo.isInTransaction()




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
        Log.i("moveSlots","$movedSlot ++++++ $otherSlot")
        repo.moveSlotAfterAnother(movedSlot,otherSlot)
    }

    fun deleteSlot(slotCode:String){
        Log.i("deleteSlot","$slotCode")
        repo.deleteSlot(slotCode)
    }
    fun endCurrendSlot(){
        Log.i("currentSlot","CURRENT SLOT")
        repo.endCurrentSlot()
    }

    fun saveTransaction() {
        Log.i("saveTransaction","VALUE: ${isInTransaction.value}")

        repo.saveTransaction()
    }

    fun abortTransaction() {
        Log.i("abortTransaction","VALUE: ${isInTransaction.value}")
        repo.abortTransaction()
    }


}
