package elite.kit.outwait.recyclerviewScreens.managmentViewScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    private val _isFragmentShowing = MutableLiveData(false)
    val isFragmentShowing: LiveData<Boolean> get() = _isFragmentShowing


    /*
    * - zuerst gebewgter slot dann der feste
    * - both delete and endCurrent
    * */
    val slotList = repo.getObservableTimeSlotList()


    fun navigateToAddSlotDialog() {
        coordinator.navigateToAddDialogFragment()
        _isFragmentShowing.value=true
    }

    fun navigateToConfigDialog() {
        coordinator.navigateToConfigDialog()
    }

    fun navigateToEditDialog(timeSlot: ClientTimeSlot) {
        coordinator.navigateToEditDialogFragment(timeSlot)
    }



}
