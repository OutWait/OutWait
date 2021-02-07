package elite.kit.outwait.recyclerviewScreens.slotDetailDialog

import android.app.AlertDialog
import android.app.Dialog
import android.database.DatabaseUtils
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import elite.kit.outwait.R
import elite.kit.outwait.databinding.AddSlotDialogFragmentBinding
import elite.kit.outwait.databinding.AddSlotDialogFragmentBinding.inflate
import elite.kit.outwait.databinding.SlotDetailDialogFragmentBinding
import elite.kit.outwait.qrCode.generator.QRCodeGenerator
import elite.kit.outwait.utils.TransformationInput
import elite.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.FixedTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.SpontaneousTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.Type
import org.joda.time.DateTime
import org.joda.time.Interval

class SlotDetailDialogFragment : DialogFragment() {


    private val viewModel: SlotDetailDialogViewModel by viewModels()
    private lateinit var binding: SlotDetailDialogFragmentBinding
    private val qrCodeGenerator: QRCodeGenerator

    init {
        qrCodeGenerator = QRCodeGenerator()
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        binding = SlotDetailDialogFragmentBinding.inflate(LayoutInflater.from(context))
        binding.viewModel = this.viewModel

        //Example Slot
        var start = DateTime(DateTime.now()).plusHours(1)
        var end = start.plusMinutes(33)
        displayProperties(FixedTimeSlot(Interval(start,end),
            "4444",
            "Müller",
            DateTime(DateTime.now().year,
                DateTime.now().monthOfYear, DateTime.now().dayOfWeek, 22, 33)))

        builder.apply {
            setView(binding.root)
            val title = setTitle(getString(R.string.slot_details))

            setPositiveButton(getString(R.string.confirm)) { dialog, which ->

            }
        }
        return builder.create()
    }

    private fun displayProperties(slot: ClientTimeSlot) {
        viewModel.isFixedSlot.value = slot.getType().ordinal == Type.FIXED_SLOT.ordinal
            when (viewModel.isFixedSlot.value) {
                true -> displayFixedSlot(slot as FixedTimeSlot)
                else -> displaySpontaneousSlot(slot as SpontaneousTimeSlot)
            }

    }

    private fun displaySpontaneousSlot(spontaneousSlot: SpontaneousTimeSlot) {
        viewModel.identifier.value=spontaneousSlot.auxiliaryIdentifier
        viewModel.interval.value=spontaneousSlot.interval
        viewModel.qrCode.value=qrCodeGenerator.generateQRCode(spontaneousSlot.slotCode)
        binding.ivQRCode.setImageBitmap(viewModel.qrCode.value)


    }

    private fun displayFixedSlot(fixedSlot: FixedTimeSlot) {
        viewModel.identifier.value=fixedSlot.auxiliaryIdentifier
        viewModel.interval.value=fixedSlot.interval
        viewModel.appointmentTime.value=fixedSlot.appointmentTime
        viewModel.qrCode.value=qrCodeGenerator.generateQRCode(fixedSlot.slotCode)
        binding.ivQRCode.setImageBitmap(viewModel.qrCode.value)

    }
}
