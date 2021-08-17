package edu.kit.outwait.recyclerviewScreens.slotDetailDialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import edu.kit.outwait.R
import edu.kit.outwait.databinding.SlotDetailDialogFragmentBinding
import edu.kit.outwait.qrCode.generator.QRCodeGenerator
import edu.kit.outwait.utils.TransformationOutput
import edu.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import edu.kit.outwait.waitingQueue.timeSlotModel.FixedTimeSlot
import edu.kit.outwait.waitingQueue.timeSlotModel.SpontaneousTimeSlot
import edu.kit.outwait.waitingQueue.timeSlotModel.Type

/**
 * Dialog to show a slot information
 *
 * @property clientTimeSlot Selected slot
 */
private const val START=0

class SlotDetailDialogFragment(private var clientTimeSlot: ClientTimeSlot) : DialogFragment() {


    private val viewModel: SlotDetailDialogViewModel by viewModels()
    private lateinit var binding: SlotDetailDialogFragmentBinding
    private val qrCodeGenerator: QRCodeGenerator = QRCodeGenerator()

    /**
     * Creates layout of detail slot and data binding
     *
     * @param savedInstanceState Passed data from previous fragment
     * @return layout with data binding
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val foregroundColorSpan = ForegroundColorSpan(
            ContextCompat.getColor(
                requireContext(),
                R.color.outwait_color
            )
        )
        val ssBuilder = SpannableStringBuilder(getString(R.string.slot_details))
        ssBuilder.setSpan(
            foregroundColorSpan,
            START,
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

    /**
     * Checks whether selected slot is a fixed slot
     *
     * @return fixed slot-> true, else false
     */
    private fun isFixedSlot(): Boolean {
        return clientTimeSlot.getType().ordinal == Type.FIXED_SLOT.ordinal
    }

    /**
     * Decides which type of slot is present
     *
     * @param slot Selected slot
     */
    private fun displayProperties(slot: ClientTimeSlot) {
            when (viewModel.isFixedSlot.value) {
                true -> displayFixedSlot(slot as FixedTimeSlot)
                else -> displaySpontaneousSlot(slot as SpontaneousTimeSlot)
            }

    }

    /**
     * Displays values of a spontaneous slot
     *
     * @param spontaneousSlot
     */
    private fun displaySpontaneousSlot(spontaneousSlot: SpontaneousTimeSlot) {
        viewModel.slotCode.value=spontaneousSlot.slotCode
        viewModel.identifier.value=spontaneousSlot.auxiliaryIdentifier
        viewModel.interval.value=TransformationOutput.intervalToString(spontaneousSlot.interval)
        viewModel.qrCode.value=qrCodeGenerator.generateQRCode(spontaneousSlot.slotCode)
        binding.ivQRCode.setImageBitmap(viewModel.qrCode.value)


    }

    /**
     * Displays values of a fixed slot
     *
     * @param fixedSlot
     */
    private fun displayFixedSlot(fixedSlot: FixedTimeSlot) {
        viewModel.slotCode.value=fixedSlot.slotCode
        viewModel.identifier.value=fixedSlot.auxiliaryIdentifier
        viewModel.interval.value=TransformationOutput.intervalToString(fixedSlot.interval)
        viewModel.appointmentTime.value=TransformationOutput.appointmentToString(fixedSlot.appointmentTime)
        viewModel.qrCode.value=qrCodeGenerator.generateQRCode(fixedSlot.slotCode)
        binding.ivQRCode.setImageBitmap(viewModel.qrCode.value)
    }
}
