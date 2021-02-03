package elite.kit.outwait.recyclerviewScreens.addSlotDialog

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddSlotDialogViewModel : ViewModel() {


    private val _identifier = MutableLiveData<String>()
    val identifier: LiveData<String>
    get() = _identifier

    private val _appointmentTime = MutableLiveData<Long>()
    val appointmentTime: LiveData<Long>
        get() = _appointmentTime

    private val _duration = MutableLiveData<Long>()
    val duration: LiveData<Long>
        get() = _duration

    private val _isFixedSlot = MutableLiveData<Boolean>()
    val isFixedSlot: LiveData<Boolean>
        get() = _isFixedSlot

    fun notifyAddSlot(duration: Double, appointmentTime: Long) {
        Log.i("viewmodel","call functions $_identifier")
    }
}
