package edu.kit.outwait.recyclerviewScreens.slotDetailDialog

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.kit.outwait.qrCode.generator.QRCodeGenerator
import org.joda.time.DateTime
import org.joda.time.Interval

/**
 * Keeps data form the slot which should be edit
 *
 */
class SlotDetailDialogViewModel : ViewModel() {
    /**
     * Entered identifier by management
     */
    val identifier = MutableLiveData<String>()
    /**
     * Slot code from selected slot by management
     */
    val slotCode = MutableLiveData<String>()
    /**
     * Entered appointment time by management
     */
    var appointmentTime = MutableLiveData<String>()
    /**
     * Entered interval by management
     */
    var interval = MutableLiveData<String>()

    /**
     * Checks whether selected slot is fixed
     */
    val isFixedSlot = MutableLiveData<Boolean>()
    /**
     * Keeps generated qr code of slotCode
     */
    val qrCode = MutableLiveData<Bitmap>()

}
