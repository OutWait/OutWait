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
class ConfigDialogViewModel @Inject constructor(private val repo: InstituteRepository) :
    ViewModel() {

    /*
    * - are getobserablepreferences set before login happen?
    * - what should errornofification should do ? why a list? if edit was wrong should it be shown?
    * - again transaction?
    * */

    //TODO check queue is emtpy to switch mode

    val standardSlotDauer = MutableLiveData<Duration>()
    var notificationTime = MutableLiveData<Duration>()
    var isModusTwo = MutableLiveData<Boolean>()
    val prioritizationTime = MutableLiveData<Duration>()
    val delayNotificationTime = MutableLiveData<Duration>()

    fun logout(){
        //TODO repo call
    }

    fun saveConfigValues(){
        //TODO repo call
    }
}
