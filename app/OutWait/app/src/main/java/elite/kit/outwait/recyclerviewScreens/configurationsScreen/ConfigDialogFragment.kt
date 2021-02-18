package elite.kit.outwait.recyclerviewScreens.configurationsScreen

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.R
import elite.kit.outwait.customDataTypes.Mode
import elite.kit.outwait.databinding.ConfigDialogFragmentBinding
import mobi.upod.timedurationpicker.TimeDurationPicker
import org.joda.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@AndroidEntryPoint
class ConfigDialogFragment : Fragment() {

    private lateinit var binding: ConfigDialogFragmentBinding
    private val viewModel: ConfigDialogViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.config_dialog_fragment, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner=this

        //Setup Dialog
//        displayValues()
        //Setup format
        setUpFormat()

        //TODO check queue empty to switch mode
        //get default values from sever
        viewModel.repo.getObservablePreferences().observe(viewLifecycleOwner, Observer {
            viewModel.standardSlotDauer.value=it.defaultSlotDuration
            viewModel.delayNotificationTime.value=it.delayNotificationTime
            viewModel.notificationTime.value=it.notificationTime
            viewModel.prioritizationTime.value=it.prioritizationTime
            viewModel.isModusTwo.value=it.mode.ordinal== Mode.TWO.ordinal
        })
        //pass new default values from user to server
        binding.btnSave.setOnClickListener {
            viewModel.standardSlotDauer.value= Duration(binding.durationStandardSlot.duration)
            viewModel.delayNotificationTime.value=Duration(binding.durationDelay.duration)
            viewModel.notificationTime.value=Duration(binding.durationNotification.duration)
            viewModel.prioritizationTime.value=Duration(binding.durationPrioritization.duration)
            viewModel.isModusTwo.value=binding.sMode.isChecked
        }

        return binding.root
    }

    private fun setUpFormat() {
        binding.durationDelay.setTimeUnits(TimeDurationPicker.HH_MM)
        binding.durationNotification.setTimeUnits(TimeDurationPicker.HH_MM)
        binding.durationPrioritization.setTimeUnits(TimeDurationPicker.HH_MM)
        binding.durationStandardSlot.setTimeUnits(TimeDurationPicker.HH_MM)
    }

    private fun displayValues() {
        binding.durationStandardSlot.duration=viewModel.standardSlotDauer.value!!.millis
        binding.durationDelay.duration=viewModel.delayNotificationTime.value!!.millis
        binding.durationPrioritization.duration=viewModel.prioritizationTime.value!!.millis
        binding.durationNotification.duration=viewModel.notificationTime.value!!.millis
        binding.sMode.isChecked= viewModel.isModusTwo.value!!
    }


}
