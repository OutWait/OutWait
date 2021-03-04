package elite.kit.outwait.recyclerviewScreens.configurationsScreen

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.customDataTypes.Mode
import elite.kit.outwait.databinding.ConfigDialogFragmentBinding
import kotlinx.android.synthetic.main.full_screen_progress_bar.*
import mobi.upod.timedurationpicker.TimeDurationPicker
import org.joda.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.time.milliseconds
import kotlin.time.toDuration

@AndroidEntryPoint
class ConfigDialogFragment : Fragment() {

    private lateinit var binding: ConfigDialogFragmentBinding
    private val viewModel: ConfigDialogViewModel by viewModels()
    private lateinit var builder: AlertDialog.Builder
    private lateinit var displayingDialog: AlertDialog
    private var firstFragmentCall=true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.config_dialog_fragment, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner



        builder = AlertDialog.Builder(activity)
        builder.apply {
            setView(R.layout.full_screen_progress_bar)
            setTitle(getString(R.string.process_title))
            setCancelable(true)
        }
        displayingDialog = builder.create()

        displayValues()

        binding.btnSave.setOnClickListener {
            emitSettingChanges()
        }

        viewModel.preferences.observe(viewLifecycleOwner) {
            binding.configStandardDuration.duration = it.defaultSlotDuration.millis
            binding.configDelayDuration.duration = it.delayNotificationTime.millis
            binding.configDurationNotification.duration = it.notificationTime.millis
            binding.configPrioDuration.duration = it.prioritizationTime.millis
            binding.sMode.isChecked = it.mode == Mode.TWO
            displayingDialog.dismiss()
            if(!firstFragmentCall){
                Toast.makeText(
                    context,
                    "Your settings are saved",
                    Toast.LENGTH_LONG
                ).show()
            }
            firstFragmentCall=false
        }

        setSwitchTextOnState(binding.sMode.isChecked)
        binding.sMode.setOnCheckedChangeListener { buttonView, isChecked ->
            setSwitchTextOnState(isChecked)
        }
        return binding.root
    }

    private fun areSettingsChanged():Boolean {
        var isSlotDurationSame =
            binding.configStandardDuration.duration == viewModel.standardSlotDuration.millis
        var isDelayNotificationTimeSame =
            binding.configDelayDuration.duration == viewModel.delayNotificationTime.millis
        var isNotificationTimeSame =
            binding.configDurationNotification.duration == viewModel.notificationTime.millis
        var isPrioDurationSame =
            binding.configPrioDuration.duration == viewModel.prioritizationTime.millis
        var isModeSame = binding.sMode.isChecked == viewModel.isModeTwo
        return !(isSlotDurationSame&&isDelayNotificationTimeSame&&isNotificationTimeSame&&isPrioDurationSame&&isModeSame)
    }

    private fun setSwitchTextOnState(isChecked: Boolean) {
        if (isChecked) {
            binding.tvSwitchText.text = "Mode 2"
        } else {
            binding.tvSwitchText.text = "Mode 1"
        }
    }

    private fun emitSettingChanges() {
        if (viewModel.isModeTwo == binding.sMode.isChecked) {
            viewModel.saveConfigValues(
                Duration(binding.configStandardDuration.duration),
                Duration(binding.configDurationNotification.duration),
                Duration(binding.configDelayDuration.duration),
                Duration(binding.configPrioDuration.duration), binding.sMode.isChecked
            )
            displayingDialog.show()
            displayingDialog.fullScreenProgressBar.indeterminateMode = true

        } else if (viewModel.slotListSize == 0 && viewModel.isModeTwo != binding.sMode.isChecked) {
            viewModel.saveConfigValues(
                Duration(binding.configStandardDuration.duration),
                Duration(binding.configDurationNotification.duration),
                Duration(binding.configDelayDuration.duration),
                Duration(binding.configPrioDuration.duration), binding.sMode.isChecked
            )
            displayingDialog.show()
            displayingDialog.fullScreenProgressBar.indeterminateMode = true


        } else {
            Toast.makeText(
                context,
                "Your queue is not empty to switch your mode",
                Toast.LENGTH_LONG
            ).show()
            Toast.makeText(
                context,
                "Your settings are not saved",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private fun displayValues() {
        binding.configStandardDuration.duration = viewModel.standardSlotDuration.millis
        binding.configDelayDuration.duration = viewModel.delayNotificationTime.millis
        binding.configDurationNotification.duration = viewModel.notificationTime.millis
        binding.configPrioDuration.duration = viewModel.prioritizationTime.millis
        binding.sMode.isChecked = viewModel.isModeTwo
    }


}
