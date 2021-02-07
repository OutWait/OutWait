package elite.kit.outwait.recyclerviewScreens.editSlotDialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
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
class EditTimeSlotDialogFragment : DialogFragment() {



    private val viewModel: EditTimeSlotDialogViewModel by viewModels()
    private lateinit var binding:EditTimeSlotDialogFragmentBinding


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = EditTimeSlotDialogFragmentBinding.inflate(LayoutInflater.from(context))
        binding.viewModel=this.viewModel
        val builder = AlertDialog.Builder(activity)

        //EXAMPLE
        var slot= FixedTimeSlot(Interval(0L,200L),"1234", "HIlfe", DateTime.now())
        binding.timeDurationInput.setTimeUnits(TimeDurationPicker.HH_MM)
        binding.tpAppointmentTime.setIs24HourView(true)
        viewModel.identifier.value=slot.auxiliaryIdentifier
        viewModel.isFixedSlot.value = isFixedSlot(slot)


        binding.tpAppointmentTime.hour= DateTime.now().hourOfDay.plus(1)
        binding.tpAppointmentTime.minute= DateTime.now().minuteOfDay
        //TODO set trough slot livedata in dialog

        builder.apply {

            setView(binding.root)
            setTitle("Edit the slot")

            setPositiveButton(getString(R.string.confirm)) { dialog, which ->
                viewModel.appointmentTime.value = TransformationInput.formatDateTime(binding.tpAppointmentTime.hour,binding.tpAppointmentTime.minute)
                viewModel.interval.value = TransformationInput.formatInterval(binding.timeDurationInput.duration)
                viewModel.notifyEditSlot()

            }

            setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.cancel()
            }

        }
        return builder.create()
    }

    private fun displaySpotaneousSlotTimes(slot: SpontaneousTimeSlot): Boolean {
        //TODO display duration time
        return false
    }

    private fun displayFixedSlotTimes(slot: FixedTimeSlot): Boolean {
        //TODO display appointmentTime and duration
        return true
    }

    private fun isFixedSlot(slot: ClientTimeSlot): Boolean {
        return when(slot.getType().ordinal) {
            Type.FIXED_SLOT.ordinal->  displayFixedSlotTimes(slot as FixedTimeSlot)
                else->displaySpotaneousSlotTimes(slot as SpontaneousTimeSlot)
        }
    }
}
