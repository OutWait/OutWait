package elite.kit.outwait.recyclerviewScreens.configurationsScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.customDataTypes.Mode
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.instituteRepository.InstituteRepository
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval
import javax.inject.Inject

@HiltViewModel
class ConfigDialogViewModel @Inject constructor( val repo: InstituteRepository, val coordinator: ConfigCoordinator) :
    ViewModel() {

     val preferences : LiveData<Preferences> =repo.getObservablePreferences()

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
        repo.changePreferences(Duration(700000L),Duration(3000L),Duration(3000L),Duration(3000L),
            true)
    }
}
