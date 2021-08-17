package edu.kit.outwait.recyclerviewScreens.addSlotDialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.color.MaterialColors.getColor
import dagger.hilt.android.AndroidEntryPoint
import edu.kit.outwait.R
import edu.kit.outwait.customDataTypes.Mode
import edu.kit.outwait.databinding.AddSlotDialogFragmentBinding
import edu.kit.outwait.recyclerviewScreens.managementViewScreen.ManagementViewFragment
import edu.kit.outwait.utils.TransformationInput
import kotlinx.android.synthetic.main.full_screen_progress_bar.*
import mobi.upod.timedurationpicker.TimeDurationPicker
import org.joda.time.DateTime
import org.joda.time.Interval

/**
 * Dialog to add a sponatneous or fixed slot
 */
private const val DEFAULT_HOUR = 0
private const val DEFAULT_MINUTE = 0
private const val START_TIME=0L
private const val START=0
private const val ONE_DAY=1
private const val EARLIER=0



@AndroidEntryPoint
class AddSlotDialogFragment : DialogFragment() {


    private val viewModel: AddSlotDialogViewModel by viewModels()
    private lateinit var binding: AddSlotDialogFragmentBinding

    /**
     *Displays a dialog to add fixed or spontaneous slot
     *
     * @param savedInstanceState Passed data from before fragment
     * @return Layout of screen
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        binding = AddSlotDialogFragmentBinding.inflate(LayoutInflater.from(context))
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
        setUpPicker()
        defaultValues()
        val foregroundColorSpan =
            ForegroundColorSpan(getColor(requireContext(), R.color.outwait_color))
        val titleBuilder = SpannableStringBuilder(getString(R.string.title_add_slot))
        titleBuilder.setSpan(
            foregroundColorSpan,
            START,
            getString(R.string.title_add_slot).length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        builder.apply {
            setView(binding.root)
            setTitle(titleBuilder)
            setPositiveButton(getString(R.string.confirm)) { dialog, which ->
                addSlot()
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.cancel()
            }
        }
        return builder.create()
    }

    /**
     * Decides depend on set values in viewModel which slot should be added
     *
     */
    private fun addSlot() {
        if (viewModel.isFixedSlot.value!!) {
            setFixedSlotValues()

            if (isDefaultAppointmentTime()) {
                Toast.makeText(
                    context,
                    getString(R.string.failure_add_slot),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                viewModel.notifyAddFixedSlot()
                ManagementViewFragment.displayingDialog.show()
                ManagementViewFragment.displayingDialog.fullScreenProgressBar.indeterminateMode =
                    true
            }

        } else {
            setSpontaneousSlotValues()
            viewModel.notifyAddSpontaneousSlot()
            ManagementViewFragment.displayingDialog.show()
            ManagementViewFragment.displayingDialog.fullScreenProgressBar.indeterminateMode = true
        }
    }

    /**
     * Set values of spontaneousSlot in viewModel
     *
     */
    private fun setSpontaneousSlotValues() {
        viewModel.interval.value =
            TransformationInput.formatInterval(binding.addSlotDuration.duration)
    }

    /**
     * Set values of fixedSlot in viewModel
     *
     */
    private fun setFixedSlotValues() {
        viewModel.interval.value =
            TransformationInput.formatInterval(binding.addSlotDuration.duration)
        viewModel.appointmentTime.value = setAppointmentIn24Interval()
        setAppointmentIn24Interval()
        Log.i("correct appointment", "${viewModel.appointmentTime.value}")
    }

    /**
     * If a slot is added which is befor the current time it will be postponed to back (24 hour later)
     *
     * @return Postponeded appointmentTime
     */
    private fun setAppointmentIn24Interval(): DateTime? {
        var isBefore = DateTime.now().compareTo(
            TransformationInput.formatDateTime(
                binding.tpAppointmentTime.hour,
                binding.tpAppointmentTime.minute
            )
        )
        var dateTimePostponed = TransformationInput.formatDateTime(
            binding.tpAppointmentTime.hour,
            binding.tpAppointmentTime.minute
        )
        if (isBefore > EARLIER) {
            dateTimePostponed = TransformationInput.formatDateTime(
                binding.tpAppointmentTime.hour,
                binding.tpAppointmentTime.minute
            ).plusDays(ONE_DAY)
        }
        return dateTimePostponed
    }

    /**
     * Checks whether passed appointmentTime is changed
     *
     * @return not changed-> true, else false
     */
    private fun isDefaultAppointmentTime(): Boolean {
        return viewModel.appointmentTime.value!!.isEqual(
            TransformationInput.formatDateTime(
                DEFAULT_HOUR,
                DEFAULT_MINUTE
            )
        )
    }

    /**
     * Sets values in the screen and in the viewModel
     *
     */
    private fun defaultValues() {
        binding.tpAppointmentTime.hour = DEFAULT_HOUR
        binding.tpAppointmentTime.minute = DEFAULT_MINUTE
        viewModel.isModeTwo.value = viewModel.preferences.value!!.mode == Mode.TWO
        viewModel.interval.value =
            Interval(START_TIME, viewModel.preferences.value!!.defaultSlotDuration.millis)
        binding.addSlotDuration.duration = viewModel.interval.value!!.toDurationMillis()
    }

    /**
     * Configs that the timedurationpicker has a 24 hour interval
     *
     */
    private fun setUpPicker() {
        binding.addSlotDuration.setTimeUnits(TimeDurationPicker.HH_MM)
        binding.tpAppointmentTime.setIs24HourView(true)
    }


}
