package elite.kit.outwait.recyclerviewScreens.editSlotDialog

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.instituteRepository.InstituteRepository
import org.joda.time.DateTime
import org.joda.time.Interval
import javax.inject.Inject

@HiltViewModel
class EditTimeSlotDialogViewModel @Inject constructor(private val repo : InstituteRepository) : ViewModel() {

    /*
    * */

    var identifier = MutableLiveData<String>()


    var appointmentTime = MutableLiveData<DateTime>()


    var interval = MutableLiveData<Interval>()


    var isFixedSlot = MutableLiveData<Boolean>()

    fun notifyEditSlot() {
        //TODO pass edit slot repo
    }

}
