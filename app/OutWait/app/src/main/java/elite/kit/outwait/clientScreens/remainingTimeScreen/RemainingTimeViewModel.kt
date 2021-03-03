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
import elite.kit.outwait.utils.TransformationOutput
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval
import javax.inject.Inject
import kotlin.time.milliseconds

@HiltViewModel
class RemainingTimeViewModel  @Inject constructor(private val repo : ClientRepository): ViewModel() {

/*
    - calculation of remainingtime here?
    - calculation of remainingTime is trough approximatedTime
*/

    //TODO calculation of remaining time
    var clientInfo = MutableLiveData<ClientInfo>()
    var clientInfoList = repo.getActiveSlots()
    private val _remainingTime = MutableLiveData<String>()
    val remainingTime
        get() = _remainingTime as LiveData<String>

init {
    //clientInfo=ClientRepository.getActiveSlots.value[0]

    //Ausgabe 20:13 also HH:mm
    TransformationOutput.intervalToString(Interval(_remainingTime.value!!))

    val slots = repo.getActiveSlots().value
    if (slots !== null && slots.isNotEmpty()){
        val approxTime = slots.last().approximatedTime
        val timeLeft = Duration(approxTime.millis - DateTime.now().millis)
        _remainingTime.value = TransformationOutput.intervalToString(Interval(timeLeft.millis))
        Log.d("RemTimeVM", "${_remainingTime.value} minuten verbleibend")
    }

    repo.getActiveSlots().observeForever {
        if (it.isNotEmpty()){
            val approxTime = it.last().approximatedTime
            val timeLeft = Duration(approxTime.millis - DateTime.now().millis)
            _remainingTime.value = TransformationOutput.intervalToString(Interval(timeLeft.millis))
            Log.d("RemTimeVM", "${_remainingTime.value} minuten verbleibend")
        }
    }
}
    fun navigateBack() {
        TODO("Not yet implemented")
    }

    fun refreshWaitingTime(){
        val showingSlot = repo.getActiveSlots().value?.last()?.slotCode ?: ""
        if (showingSlot != ""){
            repo.refreshWaitingTime(showingSlot)
        }
    }
}
