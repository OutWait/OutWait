package elite.kit.outwait.clientScreens.remainingTimeScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.instituteRepository.InstituteRepository
import org.joda.time.DateTime
import javax.inject.Inject

@HiltViewModel
class RemainingTimeViewModel  @Inject constructor(private val repo : InstituteRepository): ViewModel() {

    private lateinit var _waitingTime: MutableLiveData<String>
    val waitingTime: LiveData<String>
        get() {return _waitingTime}

    //TODO attribut for institut information

    fun navigateBack() {
        TODO("Not yet implemented")
    }

    fun refreshWaitingTime(){

    }
    // TODO: Implement the ViewModel
}
