package elite.kit.outwait.clientScreens.remainingTimeScreen

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.clientDatabase.ClientInfo
import elite.kit.outwait.clientRepository.ClientRepository
import elite.kit.outwait.instituteRepository.InstituteRepository
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval
import javax.inject.Inject

@HiltViewModel
class RemainingTimeViewModel  @Inject constructor(private val repo : ClientRepository): ViewModel() {

/*
    - calculation of remainingtime here?
    - calculation of remainingTime is trough approximatedTime
*/

    //TODO calculation of remaining time
    var clientInfo = MutableLiveData<ClientInfo>()

    private val _remainingMinutes = MutableLiveData<Int>()
    val remainingMinutes
        get() = _remainingMinutes as LiveData<Int>

init {
    //clientInfo=ClientRepository.getActiveSlots.value[0]
    val slots = repo.getActiveSlots().value
    if (slots !== null && slots.isNotEmpty()){
        val approxTime = slots.last().approximatedTime
        val timeLeft = Duration(approxTime.millis - DateTime.now().millis)
        _remainingMinutes.value = timeLeft.standardMinutes.toInt()
        Log.d("RemTimeVM", "${remainingMinutes.value} minuten verbleibend")
    }

    repo.getActiveSlots().observeForever {
        if (it.isNotEmpty()){
            val approxTime = it.last().approximatedTime
            val timeLeft = Duration(approxTime.millis - DateTime.now().millis)
            _remainingMinutes.value = timeLeft.standardMinutes.toInt()
            Log.d("RemTimeVM", "${remainingMinutes.value} minuten verbleibend")
        }
    }
}
    fun navigateBack() {
        TODO("Not yet implemented")
    }

    fun refreshWaitingTime(){

    }
}
