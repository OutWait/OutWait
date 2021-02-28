package elite.kit.outwait.recyclerviewScreens.configurationsScreen

import android.util.Log
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
class ConfigDialogViewModel @Inject constructor(
    val repo: InstituteRepository,
    val coordinator: ConfigCoordinator,
) :
    ViewModel() {

    val slotListSize = repo.getObservableTimeSlotList().value!!.size
    val preferences: LiveData<Preferences> = repo.getObservablePreferences()
    var standardSlotDuration: Duration = preferences.value!!.defaultSlotDuration
    var notificationTime: Duration = preferences.value!!.notificationTime
    var isModeTwo = preferences.value!!.mode==Mode.TWO
    val prioritizationTime: Duration = preferences.value!!.prioritizationTime
    val delayNotificationTime: Duration = preferences.value!!.delayNotificationTime


    fun logout() {
        repo.logout()
        coordinator.navigateToForwarderFragment()
    }

    fun saveConfigValues(standardSlotDuration:Duration,
                         notificationTime:Duration,
                         delayNotificationTime:Duration,
                         prioritizationTime:Duration,
                         isModeTwo:Boolean) {
        Log.i("save","success")
        repo.changePreferences(standardSlotDuration,
            notificationTime,
            delayNotificationTime,
            prioritizationTime,
            isModeTwo)

    }
}
