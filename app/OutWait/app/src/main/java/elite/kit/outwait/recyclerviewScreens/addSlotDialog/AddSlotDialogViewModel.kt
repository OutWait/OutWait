package elite.kit.outwait.recyclerviewScreens.addSlotDialog

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.joda.time.DateTime
import org.joda.time.Interval

class AddSlotDialogViewModel : ViewModel() {


     val identifier = MutableLiveData<String>()


     val appointmentTime = MutableLiveData<DateTime>()


     val duration = MutableLiveData<Interval>()


     val isFixedSlot = MutableLiveData<Boolean>()


    fun notifyAddSlot(duration: Interval, appointmentTime: DateTime) {
    }
}
