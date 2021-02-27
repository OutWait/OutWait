package elite.kit.outwait.recyclerviewScreens.addSlotDialog

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.instituteRepository.InstituteRepository
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import org.joda.time.DateTime
import org.joda.time.Interval
import javax.inject.Inject

@HiltViewModel
class AddSlotDialogViewModel @Inject constructor(val repo: InstituteRepository) :
    ViewModel() {

    val identifier = MutableLiveData("")


    var appointmentTime = MutableLiveData<DateTime>()


    var interval = MutableLiveData<Interval>()


    val isFixedSlot = MutableLiveData(false)

    var isModeTwo = MutableLiveData<Boolean>()


    val preferences = repo.getObservablePreferences()

    fun notifyAddSpontaneousSlot() {
        repo.newSpontaneousSlot(identifier.value!!,
            interval.value!!.toDuration())


        Log.i("input", "${identifier.value}\n" +
            "            ${appointmentTime.value}\n" +
            "            ${interval.value!!.toDurationMillis()}")
    }


    fun notifyAddFixedSlot() {
        repo.newFixedSlot(identifier.value!!,
            appointmentTime.value!!,
            interval.value!!.toDuration())
        Log.i("input", "${identifier.value}\n" +
            "            ${appointmentTime.value}\n" +
            "            ${interval.value!!.toDurationMillis()}")
    }
}
