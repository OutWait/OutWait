package elite.kit.outwait.recyclerviewScreens.addSlotDialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.databinding.AddSlotDialogFragmentBinding
import elite.kit.outwait.recyclerviewScreens.managementViewScreen.ManagementViewFragment
import elite.kit.outwait.utils.TransformationInput
import kotlinx.android.synthetic.main.full_screen_progress_bar.*
import mobi.upod.timedurationpicker.TimeDurationPicker

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

        viewModel.isModeTwo.value = true
        //TODO
        /*   viewModel.repo.getObservablePreferences().observe(viewLifecycleOwner, Observer {
               viewModel.interval.value = Interval(0L, it.defaultSlotDuration.millis)
               viewModel.isModeTwo.value=it.mode==Mode.TWO
           })*/

        //TODO observe slotlist if changed then dismiss dialog

        defaultValues()
        builder.apply {
            setView(binding.root)
            setTitle(getString(R.string.title_add_slot))
            setPositiveButton(getString(R.string.confirm)) { dialog, which ->

                if (viewModel.isModeTwo.value!! && viewModel.isFixedSlot.value!!) {
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
            TransformationInput.formatInterval(binding.timeDurationInput.duration)
    }

    private fun setFixedSlotValues() {
        viewModel.interval.value =
            TransformationInput.formatInterval(binding.timeDurationInput.duration)
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
        //TODO fetch duration from preferences
//        binding.timeDurationInput.duration=viewModel.interval.value!!.toDurationMillis()
        binding.timeDurationInput.duration = 3000000L
    }

    private fun setUpPicker() {
        binding.timeDurationInput.setTimeUnits(TimeDurationPicker.HH_MM)
        binding.tpAppointmentTime.setIs24HourView(true)
    }


}
