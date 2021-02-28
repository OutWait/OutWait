package elite.kit.outwait.recyclerviewScreens.editSlotDialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.databinding.EditTimeSlotDialogFragmentBinding
import elite.kit.outwait.recyclerviewScreens.managementViewScreen.ManagementViewFragment
import elite.kit.outwait.utils.TransformationInput
import elite.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.FixedTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.SpontaneousTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.Type
import kotlinx.android.synthetic.main.full_screen_progress_bar.*
import mobi.upod.timedurationpicker.TimeDurationPicker

@AndroidEntryPoint
class EditTimeSlotDialogFragment(private var editSlot: ClientTimeSlot) : DialogFragment() {


    private val viewModel: EditTimeSlotDialogViewModel by viewModels()
    private lateinit var binding: EditTimeSlotDialogFragmentBinding


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = EditTimeSlotDialogFragmentBinding.inflate(LayoutInflater.from(context))
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this

        val builder = AlertDialog.Builder(activity)
        setUpPicker()
        setValuesOfScreen(editSlot)
        viewModel.isFixedSlot.value = isFixedSlot(editSlot)
        viewModel.slotCode.value = editSlot.slotCode

        builder.apply {

            setView(binding.root)
            setTitle(getString(R.string.title_edit_dialog))

            setPositiveButton(getString(R.string.confirm)) { dialog, which ->

                if (viewModel.isFixedSlot.value!!) {
                    setFixedSlotValues()
                    viewModel.notifyEditFixedSlot()
                } else {
                    setSpontaneousSlotValues()
                    viewModel.notifyEditSpontaneousSlot()
                }
                ManagementViewFragment.displayingDialog.show()
                ManagementViewFragment.displayingDialog.fullScreenProgressBar.indeterminateMode =true
            }

            setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.cancel()
            }

        }
        return builder.create()
    }

    private fun setSpontaneousSlotValues() {
        Log.i("editSpoDur","${binding.editTimeDuration.duration}")
        viewModel.interval.value =
            TransformationInput.formatInterval(binding.editTimeDuration.duration)
    }

    private fun setFixedSlotValues() {
        viewModel.interval.value =
            TransformationInput.formatInterval(binding.editTimeDuration.duration)
        viewModel.appointmentTime.value =
            TransformationInput.formatDateTime(binding.tpAppointmentTime.hour,
                binding.tpAppointmentTime.minute)
    }

    private fun isFixedSlot(slot: ClientTimeSlot): Boolean? {
        return slot.getType().ordinal == Type.FIXED_SLOT.ordinal
    }

    private fun displaySpontaneousSlotTimes(slot: SpontaneousTimeSlot) {
        viewModel.identifier.value = slot.auxiliaryIdentifier
        viewModel.interval.value = slot.interval
        binding.editTimeDuration.duration = slot.interval.toDurationMillis()
    }

    private fun displayFixedSlotTimes(slot: FixedTimeSlot) {
        viewModel.identifier.value = slot.auxiliaryIdentifier
        viewModel.interval.value = slot.interval
        binding.editTimeDuration.duration = slot.interval.toDurationMillis()
        viewModel.appointmentTime.value = slot.appointmentTime
        binding.tpAppointmentTime.hour = slot.appointmentTime.hourOfDay
        binding.tpAppointmentTime.minute = slot.appointmentTime.minuteOfHour
    }

    private fun setValuesOfScreen(slot: ClientTimeSlot) {
        when (slot.getType().ordinal) {
            Type.FIXED_SLOT.ordinal -> displayFixedSlotTimes(slot as FixedTimeSlot)
            else -> displaySpontaneousSlotTimes(slot as SpontaneousTimeSlot)
        }
    }

    private fun setUpPicker() {
        binding.editTimeDuration.setTimeUnits(TimeDurationPicker.HH_MM)
        binding.tpAppointmentTime.setIs24HourView(true)
    }
}
