package edu.kit.outwait.recyclerviewScreens.configurationsScreen

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
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import dagger.hilt.android.AndroidEntryPoint
import edu.kit.outwait.R
import edu.kit.outwait.customDataTypes.Mode
import edu.kit.outwait.databinding.ConfigDialogFragmentBinding
import kotlinx.android.synthetic.main.full_screen_progress_bar.*
import mobi.upod.timedurationpicker.TimeDurationPicker
import org.joda.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.time.milliseconds
import kotlin.time.toDuration

/**
 * Dialog to show the configurations of the management system for the queue
 *
 */
private const val SLOT_LIST_EMPTY = 0


@AndroidEntryPoint
class ConfigDialogFragment : Fragment() {

    private lateinit var binding: ConfigDialogFragmentBinding
    private val viewModel: ConfigDialogViewModel by viewModels()
    private lateinit var builder: AlertDialog.Builder
    private lateinit var displayingDialog: AlertDialog
    private var firstFragmentCall = true

    /**
     * Builds layout and observes data from repository
     *
     * @param inflater Instance to inflate databinding layout
     * @param container Layout
     * @param savedInstanceState Passed data from former fragment
     * @return layout with data binding
     */
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
            if (!firstFragmentCall) {
                Toast.makeText(
                    context,
                    getString(R.string.text_save_config),
                    Toast.LENGTH_LONG
                ).show()
            }
            firstFragmentCall = false
        }

        setSwitchTextOnState(binding.sMode.isChecked)
        binding.sMode.setOnCheckedChangeListener { buttonView, isChecked ->
            setSwitchTextOnState(isChecked)
        }

        viewModel.slotListSize.observe(viewLifecycleOwner, Observer {
            binding.countOfClients.text=getString(R.string.text_counter)+it.size
        })

        return binding.root
    }

    /**
     * Depend on mode the text is set
     *
     * @param isChecked switch is checked-> true, else false
     */
    private fun setSwitchTextOnState(isChecked: Boolean) {
        if (isChecked) {
            binding.tvSwitchText.text = getString(R.string.modeTwo)
        } else {
            binding.tvSwitchText.text = getString(R.string.modeOne)
        }
    }

    /**
     * Emits entered data to viewModel
     *
     */
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

        } else if (viewModel.slotListSize.value!!.size== SLOT_LIST_EMPTY && viewModel.isModeTwo != binding.sMode.isChecked) {
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
                getString(R.string.text_denied_save_config),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Displays values from repository in the screen
     *
     */
    private fun displayValues() {
        binding.configStandardDuration.duration = viewModel.standardSlotDuration.millis
        binding.configDelayDuration.duration = viewModel.delayNotificationTime.millis
        binding.configDurationNotification.duration = viewModel.notificationTime.millis
        binding.configPrioDuration.duration = viewModel.prioritizationTime.millis
        binding.sMode.isChecked = viewModel.isModeTwo
    }


}
