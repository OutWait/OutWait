package elite.kit.outwait.recyclerviewScreens.managmentViewScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot

class ManagmentViewViewModel : ViewModel() {
    private var _slotList= MutableLiveData<List<TimeSlot>>()
    val slotList:LiveData<List<TimeSlot>>
        get() = _slotList

}
