package elite.kit.outwait.recyclerviewScreens.addSlotDialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.customDataTypes.Mode
import elite.kit.outwait.databinding.AddSlotDialogFragmentBinding
import elite.kit.outwait.recyclerviewScreens.managementViewScreen.ManagementViewFragment
import elite.kit.outwait.utils.TransformationInput
import kotlinx.android.synthetic.main.full_screen_progress_bar.*
import mobi.upod.timedurationpicker.TimeDurationPicker
import org.joda.time.Interval

@AndroidEntryPoint
class AddSlotDialogFragment : DialogFragment() {

    companion object {
        private const val DEFAULT_HOUR = 0
        private const val DEFAULT_MINUTE = 0
    }

    private val viewModel: AddSlotDialogViewModel by viewModels()
    private lateinit var binding: AddSlotDialogFragmentBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        binding = AddSlotDialogFragmentBinding.inflate(LayoutInflater.from(context))
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
        setUpPicker()
        defaultValues()

        //TODO set default duration trough preferences

        builder.apply {
            setView(binding.root)
            setTitle(getString(R.string.title_add_slot))
            setPositiveButton(getString(R.string.confirm)) { dialog, which ->

                if (viewModel.isFixedSlot.value!!) {
                    setFixedSlotValues()

                    if (isDefaultAppointmentTime()) {
                        Toast.makeText(context,
                            "Failed: Please enter an appointmentTime",
                            Toast.LENGTH_LONG).show()
                    } else {
                        viewModel.notifyAddFixedSlot()
                        ManagementViewFragment.displayingDialog.show()
                        ManagementViewFragment.displayingDialog.fullScreenProgressBar.indeterminateMode =true
                    }

                } else {
                    setSpontaneousSlotValues()
                    viewModel.notifyAddSpontaneousSlot()
                    ManagementViewFragment.displayingDialog.show()
                    ManagementViewFragment.displayingDialog.fullScreenProgressBar.indeterminateMode =true
                }
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.cancel()
            }
        }
        return builder.create()
    }

    private fun setSpontaneousSlotValues() {
        viewModel.interval.value =
            TransformationInput.formatInterval(binding.addSlotDuration.duration)
    }

    private fun setFixedSlotValues() {
        Log.i("minuteADD","${binding.tpAppointmentTime.minute}")
        viewModel.interval.value =
            TransformationInput.formatInterval(binding.addSlotDuration.duration)
        viewModel.appointmentTime.value =
            TransformationInput.formatDateTime(binding.tpAppointmentTime.hour,
                binding.tpAppointmentTime.minute)
    }

    private fun isDefaultAppointmentTime(): Boolean {
        return viewModel.appointmentTime.value!!.isEqual(TransformationInput.formatDateTime(
            DEFAULT_HOUR,
            DEFAULT_MINUTE))
    }

    private fun defaultValues() {
        binding.tpAppointmentTime.hour = DEFAULT_HOUR
        binding.tpAppointmentTime.minute = DEFAULT_MINUTE

        viewModel.isModeTwo.value=viewModel.preferences.value!!.mode==Mode.TWO
        viewModel.interval.value=Interval(0L,viewModel.preferences.value!!.defaultSlotDuration.millis)

        binding.addSlotDuration.duration = viewModel.interval.value!!.toDurationMillis()
    }

    private fun setUpPicker() {
        binding.addSlotDuration.setTimeUnits(TimeDurationPicker.HH_MM)
        binding.tpAppointmentTime.setIs24HourView(true)
    }


}
