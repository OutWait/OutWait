package edu.kit.outwait.recyclerviewScreens.managementViewScreen

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.kit.outwait.instituteRepository.InstituteRepository
import edu.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import javax.inject.Inject

/**
 * Keeps all data from managementViewFragment
 *
 * @property repo Instance of institute repository
 * @property coordinator Executes navigation
 */
@HiltViewModel
class ManagementViewViewModel @Inject constructor(
     val repo: InstituteRepository,
    private val coordinator: ManagementViewCoordinator,
) : ViewModel() {
    /**
     * Monitors current slot list (queue) from repository
     */
    val slotQueue= MediatorLiveData<List<TimeSlot>>().apply {
        addSource(repo.getObservableTimeSlotList()) {
            value = it
        }
    }

    var isLoggedIn = repo.isLoggedIn()

    /**
     * Monitors current state of transaction
     */
    var isInTransaction= repo.isInTransaction()

    /**
     * Navigates to addSlotDialog
     *
     */
    fun navigateToAddSlotDialog() {
        coordinator.navigateToAddDialogFragment()
    }

    /**
     * Navigates to configDialog
     *
     */
    fun navigateToConfigDialog() {
        coordinator.navigateToConfigDialog()
    }

    /**
     * Notifies a movement of a slot
     *
     * @param movedSlot Slot which is moved
     * @param otherSlot Slot which is before the moved slot
     */
    fun moveSlotAfterAnother(movedSlot: String, otherSlot: String) {
        repo.moveSlotAfterAnother(movedSlot,otherSlot)
    }

    /**
     * Notifies a slot is deleted
     *
     * @param slotCode Slot code of the deleted slot
     */
    fun deleteSlot(slotCode:String){
        repo.deleteSlot(slotCode)
    }

    /**
     * Notifies that the first slot is deleted
     *
     */
    fun deleteCurrentSlot(){
        repo.endCurrentSlot()
    }

    /**
     * Notifies that changes are saved from the transaction
     *
     */
    fun saveTransaction() {
        repo.saveTransaction()
    }

    /**
     * Notifies that changes are aborted from the transaction
     *
     */
    fun abortTransaction() {
        repo.abortTransaction()
    }
}
