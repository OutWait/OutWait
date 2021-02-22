package elite.kit.outwait.recyclerviewScreens.configurationsScreen

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.instituteRepository.InstituteRepository
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval
import javax.inject.Inject

@HiltViewModel
class ConfigDialogViewModel @Inject constructor( val repo: InstituteRepository, val coordinator: ConfigCoordinator) :
    ViewModel() {

    /*
    * - preferences never null, values first from server
    * - standardduration show in addslotfragment
    * */

    //TODO check queue is emtpy to switch mode

    val standardSlotDauer = MutableLiveData<Duration>()
    var notificationTime = MutableLiveData<Duration>()
    var isModusTwo = MutableLiveData<Boolean>()
    val prioritizationTime = MutableLiveData<Duration>()
    val delayNotificationTime = MutableLiveData<Duration>()

    fun logout(){
        //TODO coordiantor to go forwarder fragment
        repo.logout()
        coordinator.navigateToForwarderFragment()
    }

    fun saveConfigValues(){
        //TODO check data on validation
        repo.changePreferences(standardSlotDauer.value!!,notificationTime.value!!,delayNotificationTime.value!!,prioritizationTime.value!!,isModusTwo.value!!)

    }
}
