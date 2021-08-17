package edu.kit.outwait.clientScreens.remainingTimeScreen

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.kit.outwait.clientDatabase.ClientInfo
import edu.kit.outwait.clientRepository.ClientRepository
import edu.kit.outwait.utils.TransformationOutput
import org.joda.time.DateTime
import org.joda.time.Duration
import javax.inject.Inject

private const val TWO_DAYS = 172800000L
private const val ONE_SEC = 1000L

/**
 * Abstraction of the screen where the client can see his waiting time.
 *
 * @property repo
 * @property coordinator
 */
@HiltViewModel
class RemainingTimeViewModel @Inject constructor(
    private val repo: ClientRepository,
    private var coordinator: RemainingTimeCoordinator
) : ViewModel() {

    /**
     * List that contains all active client slots
     */
    val clientInfoList = MediatorLiveData<List<ClientInfo>>().apply {
        addSource(repo.getActiveSlots()) {
            value = it
        }
    }

    /**
     * Live Data containing the institutes name
     */
    var instituteName: MediatorLiveData<String> = MediatorLiveData<String>().apply {
        addSource(repo.getActiveSlots()) {
            if (!it.isNullOrEmpty()) {
                value = it.first().institutionName
            }
        }
    }

    private var approximatedTime: DateTime? = null

    private val _remainingTime = MutableLiveData("")
    val remainingTime get() = _remainingTime as LiveData<String>


    /*
    The ViewModel refreshes the waiting Time every second,
    it has to calculate the difference between the approximated
    time and the current time
     */
    private val timer = object : CountDownTimer(TWO_DAYS, ONE_SEC) {
        override fun onTick(millisUntilFinished: Long) {
            if (!repo.isConnectedToServer()){
                _remainingTime.value = "?"
                return
            }

            if (approximatedTime !== null) {
                val now = DateTime.now()
                var diff = Duration(
                    approximatedTime!!.millis - now.millis
                )
                //Round Time to full minute
                if (diff.standardSeconds > 30){
                    diff += Duration.standardSeconds(30)
                }

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

    /**
     * Delegates to the repo that the client manually wants to receive new waiting time
     * information
     *
     */
    fun refreshWaitingTime() {
        val showingSlot = repo.getActiveSlots().value?.last()?.slotCode ?: ""
        if (showingSlot != "") {
            repo.refreshWaitingTime(showingSlot)
        }
    }


}
