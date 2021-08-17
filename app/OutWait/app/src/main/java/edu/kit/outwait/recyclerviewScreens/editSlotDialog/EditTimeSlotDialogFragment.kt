package edu.kit.outwait.recyclerviewScreens.editSlotDialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import edu.kit.outwait.R
import edu.kit.outwait.databinding.EditTimeSlotDialogFragmentBinding
import edu.kit.outwait.recyclerviewScreens.managementViewScreen.ManagementViewFragment
import edu.kit.outwait.utils.TransformationInput
import edu.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import edu.kit.outwait.waitingQueue.timeSlotModel.FixedTimeSlot
import edu.kit.outwait.waitingQueue.timeSlotModel.SpontaneousTimeSlot
import edu.kit.outwait.waitingQueue.timeSlotModel.Type
import kotlinx.android.synthetic.main.full_screen_progress_bar.*
import mobi.upod.timedurationpicker.TimeDurationPicker
import java.time.Duration

/**
 * Dialog to edit a fixed or spontaneous slot
 *
 * @property editSlot
 */
private const val START = 0
private const val SAME = 0


@AndroidEntryPoint
class EditTimeSlotDialogFragment(private var editSlot: ClientTimeSlot) : DialogFragment() {


    private val viewModel: EditTimeSlotDialogViewModel by viewModels()
    private lateinit var binding: EditTimeSlotDialogFragmentBinding

    /**
     * Displays a dialog and before it, values of the selected slot are shown
     *
     * @param savedInstanceState Passed data from previous fragment
     * @return layout with data binding
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = EditTimeSlotDialogFragmentBinding.inflate(LayoutInflater.from(context))
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this

        val builder = AlertDialog.Builder(activity)
        setUpPicker()
        setValuesOfScreen(editSlot)
        viewModel.isFixedSlot.value = isFixedSlot()
        viewModel.slotCode.value = editSlot.slotCode

        val foregroundColorSpan = ForegroundColorSpan(
            ContextCompat.getColor(
                requireContext(),
                R.color.outwait_color
            )
        )
        val ssBuilder = SpannableStringBuilder(getString(R.string.title_edit_dialog))
        ssBuilder.setSpan(
            foregroundColorSpan,
            START,
            getString(R.string.title_edit_dialog).length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        builder.apply {
            setView(binding.root)
            setTitle(ssBuilder)
            setPositiveButton(getString(R.string.confirm)) { dialog, which ->
                if (isSlotEdit()) {

                    if (viewModel.isFixedSlot.value!!) {
                        setFixedSlotValues()
                        viewModel.notifyEditFixedSlot()
                    } else {
                        setSpontaneousSlotValues()
                        viewModel.notifyEditSpontaneousSlot()
                    }
                    ManagementViewFragment.displayingDialog.show()
                    ManagementViewFragment.displayingDialog.fullScreenProgressBar.indeterminateMode =
                        true
                }
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.cancel()
            }

        }
        return builder.create()
    }

    /**
     * Checks whether a slot is edit
     *
     * @return slot edit-> true, else false
     */
    private fun isSlotEdit(): Boolean {
        var isIdentifierSame = editSlot.auxiliaryIdentifier.compareTo(viewModel.identifier.value!!)
        var isIntervalSame = editSlot.interval.toDurationMillis()
            .compareTo(binding.editTimeDurationEditDialog.duration)
        var isAppointmentSame = true
        if (isFixedSlot()) {
            var isHourSame =
                (editSlot as FixedTimeSlot).appointmentTime.hourOfDay.toString() == binding.tpAppointmentTimeEdit.hour.toString()
            var isMinuteSame =
                (editSlot as FixedTimeSlot).appointmentTime.minuteOfHour == binding.tpAppointmentTimeEdit.minute
            isAppointmentSame = isHourSame && isMinuteSame
        }
        return !(isIdentifierSame == SAME && isIntervalSame == SAME && isAppointmentSame)
    }

    /**
     * Sets interval of a spontaneous slot
     *
     */
    private fun setSpontaneousSlotValues() {
        viewModel.interval.value =
            TransformationInput.formatInterval(binding.editTimeDurationEditDialog.duration)
    }

    /**
     * Sets value of a fixed slot
     *
     */
    private fun setFixedSlotValues() {
        viewModel.interval.value =
            TransformationInput.formatInterval(binding.editTimeDurationEditDialog.duration)
        viewModel.appointmentTime.value =
            TransformationInput.formatDateTime(
                binding.tpAppointmentTimeEdit.hour,
                binding.tpAppointmentTimeEdit.minute
            )
    }

    /**
     * Checks whether selected slot is a fixed one
     *
     * @return
     */
    private fun isFixedSlot(): Boolean {
        return editSlot.getType().ordinal == Type.FIXED_SLOT.ordinal
    }

    /**
     * Displays values of a spontaneous slot
     *
     * @param slot Selected slot
     */
    private fun displaySpontaneousSlotTimes(slot: SpontaneousTimeSlot) {
        viewModel.identifier.value = slot.auxiliaryIdentifier
        viewModel.interval.value = slot.interval
        binding.editTimeDurationEditDialog.duration = slot.interval.toDurationMillis()
    }

    /**
     * Displays values of a fixed slot
     *
     * @param slot Selected slot
     */
    private fun displayFixedSlotTimes(slot: FixedTimeSlot) {
        viewModel.identifier.value = slot.auxiliaryIdentifier
        viewModel.interval.value = slot.interval
        binding.editTimeDurationEditDialog.duration = slot.interval.toDurationMillis()
        viewModel.appointmentTime.value = slot.appointmentTime
        binding.tpAppointmentTimeEdit.hour = slot.appointmentTime.hourOfDay
        binding.tpAppointmentTimeEdit.minute = slot.appointmentTime.minuteOfHour
    }

    /**
     * Decides which type of slot is selected and displays its values
     *
     * @param slot Selected slot
     */
    private fun setValuesOfScreen(slot: ClientTimeSlot) {
        when (slot.getType().ordinal) {
            Type.FIXED_SLOT.ordinal -> displayFixedSlotTimes(slot as FixedTimeSlot)
            else -> displaySpontaneousSlotTimes(slot as SpontaneousTimeSlot)
        }
    }

    /**
     * Sets the time duration picker and appointment time picker in 24 hour format
     *
     */
    private fun setUpPicker() {
        binding.editTimeDurationEditDialog.setTimeUnits(TimeDurationPicker.HH_MM)
        binding.tpAppointmentTimeEdit.setIs24HourView(true)
    }
}
