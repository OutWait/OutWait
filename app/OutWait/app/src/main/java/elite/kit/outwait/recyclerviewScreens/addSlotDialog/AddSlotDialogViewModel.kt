package elite.kit.outwait.recyclerviewScreens.addSlotDialog

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.joda.time.DateTime
import org.joda.time.Interval

class AddSlotDialogViewModel : ViewModel() {


    private val _identifier = MutableLiveData<String>()
    val identifier: LiveData<String>
    get() = _identifier

    private val _appointmentTime = MutableLiveData<DateTime>()
    val appointmentTime: LiveData<DateTime>
        get() = _appointmentTime

    private val _duration = MutableLiveData<Interval>()
    val duration: LiveData<Interval>
        get() = _duration

    private val _isFixedSlot = MutableLiveData<Boolean>()
    val isFixedSlot: LiveData<Boolean>
        get() = _isFixedSlot

    fun notifyAddSlot(duration: Interval, appointmentTime: DateTime) {
        Log.i("viewmodel","call functions $_identifier")
    }
}
