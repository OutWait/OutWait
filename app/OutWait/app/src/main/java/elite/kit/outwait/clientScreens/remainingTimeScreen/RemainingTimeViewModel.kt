package elite.kit.outwait.clientScreens.remainingTimeScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.clientDatabase.ClientInfo
import elite.kit.outwait.instituteRepository.InstituteRepository
import org.joda.time.DateTime
import org.joda.time.Interval
import javax.inject.Inject

@HiltViewModel
class RemainingTimeViewModel  @Inject constructor(private val repo : InstituteRepository): ViewModel() {

/*
    - calculation of remainingtime here?
    - calculation of remainingTime is trough approximatedTime
*/

    //TODO calculation of remaining time
    var clientInfo = MutableLiveData<ClientInfo>()

init {
    //clientInfo=ClientRepository.getActiveSlots.value[0]
}
    fun navigateBack() {
        TODO("Not yet implemented")
    }

    fun refreshWaitingTime(){

    }
}
