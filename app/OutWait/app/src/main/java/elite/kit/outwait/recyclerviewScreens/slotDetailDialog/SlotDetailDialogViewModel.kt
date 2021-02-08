package elite.kit.outwait.recyclerviewScreens.slotDetailDialog

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import elite.kit.outwait.qrCode.generator.QRCodeGenerator
import org.joda.time.DateTime
import org.joda.time.Interval

class SlotDetailDialogViewModel : ViewModel() {

    val identifier = MutableLiveData<String>()

    val slotCode = MutableLiveData<String>()

    var appointmentTime = MutableLiveData<DateTime>()


    var interval = MutableLiveData<Interval>()


    val isFixedSlot = MutableLiveData<Boolean>()

    val qrCode = MutableLiveData<Bitmap>()




}
