package edu.kit.outwait.recyclerviewScreens.addSlotDialog

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.kit.outwait.customDataTypes.Preferences
import edu.kit.outwait.instituteRepository.InstituteRepository
import edu.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import org.joda.time.DateTime
import org.joda.time.Interval
import javax.inject.Inject

/**
 * This is the viewModel of addSlotDialogFragment which keeps data for it
 *
 * @property repo
 */
@HiltViewModel
class AddSlotDialogViewModel @Inject constructor(val repo: InstituteRepository) :
    ViewModel() {
    /**
     * Entered identifier of a slot by management
     */
    val identifier = MutableLiveData("")

    /**
     * Entered appointment time of a slot by management
     */
    var appointmentTime = MutableLiveData<DateTime>()

    /**
     * Entered interval of a slot by management
     */
    var interval = MutableLiveData<Interval>()

    /**
     * Checks whether entered slot is a fixed one
     */
    val isFixedSlot = MutableLiveData(false)
    /**
     * Checks whether management is in second mode
     */
    var isModeTwo = MutableLiveData<Boolean>()

    /**
     * Gets default preferences from server which are entered by management
     */
    val preferences = repo.getObservablePreferences()

    /**
     * Notifies that a spontaneous slot is added
     *
     */
    fun notifyAddSpontaneousSlot() {
        repo.newSpontaneousSlot(identifier.value!!,
            interval.value!!.toDuration())
    }

    /**
     * Notifies that a fixed slot is added
     *
     */
    fun notifyAddFixedSlot() {
        repo.newFixedSlot(identifier.value!!,
            appointmentTime.value!!,
            interval.value!!.toDuration())
    }
}
