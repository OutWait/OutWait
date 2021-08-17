package edu.kit.outwait.recyclerviewScreens.editSlotDialog

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.kit.outwait.instituteRepository.InstituteRepository
import org.joda.time.DateTime
import org.joda.time.Interval
import javax.inject.Inject

/**
 * Keeps data for the editTimeSlotDialogFragment
 *
 * @property repo Instance to institute repository
 */
@HiltViewModel
class EditTimeSlotDialogViewModel @Inject constructor(private val repo: InstituteRepository) :
    ViewModel() {
    /**
     * Entered identifier of a slot by management
     */
    var identifier = MutableLiveData<String>()

    /**
     * Entered appointment time of a slot by management
     */
    var appointmentTime = MutableLiveData<DateTime>()

    /**
     * Entered interval of a slot by management
     */
    var interval = MutableLiveData<Interval>()

    /**
     * State whether slot is a fixed one
     */
    var isFixedSlot = MutableLiveData<Boolean>()

    /**
     * Keeps the slot code of a slot
     */
    var slotCode = MutableLiveData<String>()

    /**
     * Notifies whether a spontaneous slot is edit
     *
     */
    fun notifyEditSpontaneousSlot() {
        repo.changeSpontaneousSlotInfo(
            slotCode.value!!,
            interval.value!!.toDuration(), identifier.value!!
        )
    }

    /**
     * Notifies whether a fixed slot is edit
     *
     */
    fun notifyEditFixedSlot() {
        repo.changeFixedSlotInfo(
            slotCode.value!!,
            interval.value!!.toDuration(),
            identifier.value!!,
            appointmentTime.value!!
        )
    }

}
