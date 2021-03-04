package elite.kit.outwait.clientScreens.remainingTimeScreen

import android.os.CountDownTimer
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

private const val TWO_DAYS = 172800000L
private const val ONE_SEC = 1000L

@HiltViewModel
class RemainingTimeViewModel @Inject constructor(
    private val repo: ClientRepository,
    private var coordinator: RemainingTimeCoordinator
) : ViewModel() {

    val clientInfoList = repo.getActiveSlots()
    var instituteName = MutableLiveData(clientInfoList.value!!.first().institutionName)

    private var approximatedTime: DateTime? = null

    private val _remainingTime = MutableLiveData<String>("")
    val remainingTime get() = _remainingTime as LiveData<String>



    private val timer = object : CountDownTimer(TWO_DAYS, ONE_SEC) {
        override fun onTick(millisUntilFinished: Long) {
            if (approximatedTime !== null) {
                val now = DateTime.now()
                val diff = Duration(
                    approximatedTime!!.millis - now.millis
                )

                if (diff > Duration(0))
                    _remainingTime.value = TransformationOutput.durationToString(diff)
                else {
                    _remainingTime.value = TransformationOutput.durationToString(Duration(0))
                }
            }
        }

        override fun onFinish() {
        }

    }


    init {
        timer.start()
        repo.getActiveSlots().observeForever {
            if (it !== null) {
                if (it.isNotEmpty()) {
                    approximatedTime = it.last().approximatedTime
                }
            }
        }

    }


    fun refreshWaitingTime() {
        val showingSlot = repo.getActiveSlots().value?.last()?.slotCode ?: ""
        if (showingSlot != "") {
            repo.refreshWaitingTime(showingSlot)
        }
    }

    fun navigateBack() {
        coordinator.navigateToForwarderFragment()
    }
}
