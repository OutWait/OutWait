package elite.kit.outwait.recyclerviewScreens.addSlotDialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.viewModels
import elite.kit.outwait.R
import elite.kit.outwait.databinding.AddSlotDialogFragmentBinding
import elite.kit.outwait.utils.TransformationInput
import mobi.upod.timedurationpicker.TimeDurationPicker
import org.joda.time.DateTime
import org.joda.time.DateTimeFieldType.hourOfDay
import kotlin.time.toDuration

class AddSlotDialogFragment : AppCompatDialogFragment() {

    private  val viewModel: AddSlotDialogViewModel by viewModels()
    private lateinit var binding: AddSlotDialogFragmentBinding



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        binding = AddSlotDialogFragmentBinding.inflate(LayoutInflater.from(context))



        binding.viewModel = this.viewModel
        binding.tpAppointmentTime.setIs24HourView(true)
        //Default time is now time
        binding.tpAppointmentTime.hour= DateTime.now().hourOfDay.plus(1)
        binding.tpAppointmentTime.minute= DateTime.now().minuteOfDay
        binding.timeDurationInput.setTimeUnits(TimeDurationPicker.HH_MM)
        viewModel.isModeTwo.value=true

        //TODO FixedSlot only possible in Modus 2 - binding.cbIsFixedSlot.isEnabled & appointment timepicker disable

        builder.apply {

            setView(binding.root)
            setTitle(getString(R.string.title_add_slot))

            setPositiveButton(getString(R.string.confirm)) { dialog, which ->
                viewModel.appointmentTime.value = TransformationInput.formatDateTime(binding.tpAppointmentTime.hour,binding.tpAppointmentTime.minute)
                viewModel.interval.value = TransformationInput.formatInterval(binding.timeDurationInput.duration)
                viewModel.notifyAddSlot()
            }

            setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.cancel()
            }

        }
        return builder.create()

    }
}
