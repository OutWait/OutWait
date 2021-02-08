package elite.kit.outwait.recyclerviewScreens.editSlotDialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.databinding.EditTimeSlotDialogFragmentBinding
import elite.kit.outwait.utils.TransformationInput
import elite.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.FixedTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.SpontaneousTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.Type
import mobi.upod.timedurationpicker.TimeDurationPicker
import org.joda.time.DateTime
import org.joda.time.Interval

@AndroidEntryPoint
class EditTimeSlotDialogFragment(private var editSlot: ClientTimeSlot) : DialogFragment() {


    private val viewModel: EditTimeSlotDialogViewModel by viewModels()
    private lateinit var binding: EditTimeSlotDialogFragmentBinding
    //private val args: EditTimeSlotDialogFragmentArgs by navArgs()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = EditTimeSlotDialogFragmentBinding.inflate(LayoutInflater.from(context))
        binding.viewModel = this.viewModel
        val builder = AlertDialog.Builder(activity)

        //EXAMPLE
        var slot = editSlot
        setUpPicker()
        setValuesOfScreen(editSlot)
        viewModel.isFixedSlot.value = isFixedSlot(slot)
        builder.apply {

            setView(binding.root)
            setTitle(getString(R.string.title_edit_dialog))

            setPositiveButton(getString(R.string.confirm)) { dialog, which ->

                if (viewModel.isFixedSlot.value!!) {
                    viewModel.interval.value =
                        TransformationInput.formatInterval(binding.timeDurationInput.duration)
                    viewModel.appointmentTime.value =
                        TransformationInput.formatDateTime(binding.tpAppointmentTime.hour,
                            binding.tpAppointmentTime.minute)
                } else {
                    viewModel.interval.value =
                        TransformationInput.formatInterval(binding.timeDurationInput.duration)
                }
                viewModel.notifyEditSlot()
            }

            setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.cancel()
            }

        }
        return builder.create()
    }

    private fun isFixedSlot(slot: ClientTimeSlot): Boolean? {
        return slot.getType().ordinal == Type.FIXED_SLOT.ordinal
    }

    private fun displaySpotaneousSlotTimes(slot: SpontaneousTimeSlot) {
        viewModel.identifier.value = slot.auxiliaryIdentifier
        viewModel.interval.value = slot.interval
        binding.timeDurationInput.duration = slot.interval.toDurationMillis()
    }

    private fun displayFixedSlotTimes(slot: FixedTimeSlot) {
        viewModel.identifier.value = slot.auxiliaryIdentifier
        viewModel.interval.value = slot.interval
        binding.timeDurationInput.duration = slot.interval.toDurationMillis()
        viewModel.appointmentTime.value = slot.appointmentTime
        binding.tpAppointmentTime.hour = slot.appointmentTime.hourOfDay
        binding.tpAppointmentTime.minute = slot.appointmentTime.minuteOfHour
    }

    private fun setValuesOfScreen(slot: ClientTimeSlot) {
        when (slot.getType().ordinal) {
            Type.FIXED_SLOT.ordinal -> displayFixedSlotTimes(slot as FixedTimeSlot)
            else -> displaySpotaneousSlotTimes(slot as SpontaneousTimeSlot)
        }
    }

    private fun setUpPicker() {
        binding.timeDurationInput.setTimeUnits(TimeDurationPicker.HH_MM)
        binding.tpAppointmentTime.setIs24HourView(true)
    }
}
