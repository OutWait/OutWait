package elite.kit.outwait.recyclerviewScreens.addSlotDialog

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
}
