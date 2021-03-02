package elite.kit.outwait.recyclerviewScreens.slotDetailDialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import elite.kit.outwait.R
import elite.kit.outwait.databinding.SlotDetailDialogFragmentBinding
import elite.kit.outwait.qrCode.generator.QRCodeGenerator
import elite.kit.outwait.utils.TransformationOutput
import elite.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.FixedTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.SpontaneousTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.Type


class SlotDetailDialogFragment(private var clientTimeSlot: ClientTimeSlot) : DialogFragment() {


    private val viewModel: SlotDetailDialogViewModel by viewModels()
    private lateinit var binding: SlotDetailDialogFragmentBinding
    private val qrCodeGenerator: QRCodeGenerator = QRCodeGenerator()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //TODO Fragment design wrong, appointmenttime not showing


        // Initialize a new foreground color span instance

        // Initialize a new foreground color span instance
        val foregroundColorSpan = ForegroundColorSpan(Color.parseColor("#38B6FF"))

        // Initialize a new spannable string builder instance
        // Initialize a new spannable string builder instance
        val ssBuilder = SpannableStringBuilder(getString(R.string.slot_details))

        // Apply the text color span

        // Apply the text color span
        ssBuilder.setSpan(
            foregroundColorSpan,
            0,
            getString(R.string.slot_details).length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )


        val builder = AlertDialog.Builder(activity)
        binding = SlotDetailDialogFragmentBinding.inflate(LayoutInflater.from(context))
        binding.viewModel = this.viewModel
        binding.lifecycleOwner=this

        viewModel.isFixedSlot.value = isFixedSlot()
        displayProperties(clientTimeSlot)

        builder.apply {
            setView(binding.root)
            setTitle(ssBuilder)
            setPositiveButton(getString(R.string.confirm)) { dialog, which ->
                dialog.dismiss()
            }
        }
        return builder.create()
    }

    private fun isFixedSlot(): Boolean {
        return clientTimeSlot.getType().ordinal == Type.FIXED_SLOT.ordinal
    }

    private fun displayProperties(slot: ClientTimeSlot) {
            when (viewModel.isFixedSlot.value) {
                true -> displayFixedSlot(slot as FixedTimeSlot)
                else -> displaySpontaneousSlot(slot as SpontaneousTimeSlot)
            }

    }

    private fun displaySpontaneousSlot(spontaneousSlot: SpontaneousTimeSlot) {
        viewModel.slotCode.value=spontaneousSlot.slotCode
        viewModel.identifier.value=spontaneousSlot.auxiliaryIdentifier
        Log.i("stringAddDialog","${spontaneousSlot.interval.toString()}")
        viewModel.interval.value=TransformationOutput.intervalToString(spontaneousSlot.interval)
        Log.i("stringAddDialog2", TransformationOutput.intervalToString(spontaneousSlot.interval))

        viewModel.qrCode.value=qrCodeGenerator.generateQRCode(spontaneousSlot.slotCode)
        binding.ivQRCode.setImageBitmap(viewModel.qrCode.value)


    }

    private fun displayFixedSlot(fixedSlot: FixedTimeSlot) {
        viewModel.slotCode.value=fixedSlot.slotCode
        viewModel.identifier.value=fixedSlot.auxiliaryIdentifier
        viewModel.interval.value=TransformationOutput.intervalToString(fixedSlot.interval)
        viewModel.appointmentTime.value=TransformationOutput.appointmentToString(fixedSlot.appointmentTime)
        viewModel.qrCode.value=qrCodeGenerator.generateQRCode(fixedSlot.slotCode)
        binding.ivQRCode.setImageBitmap(viewModel.qrCode.value)
    }
}
