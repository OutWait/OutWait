package elite.kit.outwait.recyclerviewScreens.addSlotDialog

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.joda.time.DateTime
import org.joda.time.Interval

class AddSlotDialogViewModel : ViewModel() {


     val identifier = MutableLiveData<String>()


     var appointmentTime = MutableLiveData<DateTime>()


     var interval = MutableLiveData<Interval>()


     val isFixedSlot = MutableLiveData<Boolean>()

    var isModeTwo=MutableLiveData<Boolean>()


    fun notifyAddSlot() {
        //TODO check mode
        //TODO pass slot
        Log.i("addSlot","\n ${identifier.value} \n ${appointmentTime.value} \n ${interval.value} \n ${isFixedSlot.value}")
    }

    //TODO add observer to disable entering fixedslot
}
