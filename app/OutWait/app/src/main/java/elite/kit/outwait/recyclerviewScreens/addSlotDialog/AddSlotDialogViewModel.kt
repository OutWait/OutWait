package elite.kit.outwait.recyclerviewScreens.addSlotDialog

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.instituteRepository.InstituteRepository
import org.joda.time.DateTime
import org.joda.time.Interval
import javax.inject.Inject
@HiltViewModel
class AddSlotDialogViewModel @Inject constructor(private val repo : InstituteRepository) : ViewModel() {

    /*
    * - only pass of new data is enough?
    * - trough live data queue gets new info ?
    * - how should transaction happen?
    * */

     val identifier = MutableLiveData<String>()


     var appointmentTime = MutableLiveData<DateTime>()


     var interval = MutableLiveData<Interval>()


     val isFixedSlot = MutableLiveData<Boolean>()

    var isModeTwo=MutableLiveData<Boolean>()

    init {
        isFixedSlot.value=false
    }

    //TODO edit visibility of fixedslot trough isModeTwo && isFixedSlot

    fun notifyAddSlot() {
        //TODO check mode and type of slot
        //TODO pass slot
    }
}
