package elite.kit.outwait.recyclerviewScreens.addSlotDialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.databinding.AddSlotDialogFragmentBinding
import elite.kit.outwait.utils.TransformationInput
import elite.kit.outwait.waitingQueue.timeSlotModel.FixedTimeSlot
import mobi.upod.timedurationpicker.TimeDurationPicker
import org.joda.time.DateTime
import org.joda.time.DateTimeFieldType.hourOfDay
import kotlin.time.toDuration
@AndroidEntryPoint
class AddSlotDialogFragment : DialogFragment() {

    companion object{
    private const val DEFAULT_HOUR=0
        private const val DEFAULT_MINUTE=0
    }
    private  val viewModel: AddSlotDialogViewModel by viewModels()
    private lateinit var binding: AddSlotDialogFragmentBinding



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        binding = AddSlotDialogFragmentBinding.inflate(LayoutInflater.from(context))
        binding.viewModel = this.viewModel

        setUpPicker()
        displayDefaultAppointmentTime()

        //TODO fetch mode of vm of managementView
        viewModel.isModeTwo.value=false

        builder.apply {

            setView(binding.root)
            setTitle(getString(R.string.title_add_slot))

            setPositiveButton(getString(R.string.confirm)) { dialog, which ->
                if (viewModel.isModeTwo.value!! && viewModel.isFixedSlot.value!!) {
                    viewModel.interval.value =
                        TransformationInput.formatInterval(binding.timeDurationInput.duration)
                    viewModel.appointmentTime.value =
                        TransformationInput.formatDateTime(binding.tpAppointmentTime.hour,
                            binding.tpAppointmentTime.minute)
                } else {
                    viewModel.interval.value =
                        TransformationInput.formatInterval(binding.timeDurationInput.duration)
                }
                viewModel.notifyAddSlot()
            }

            setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.cancel()
            }

        }
        return builder.create()

    }

    private fun setUpPicker() {
        binding.timeDurationInput.setTimeUnits(TimeDurationPicker.HH_MM)
        binding.tpAppointmentTime.setIs24HourView(true)
    }

    private fun displayDefaultAppointmentTime() {
        binding.tpAppointmentTime.hour = DEFAULT_HOUR
        binding.tpAppointmentTime.minute = DEFAULT_MINUTE
    }
}
