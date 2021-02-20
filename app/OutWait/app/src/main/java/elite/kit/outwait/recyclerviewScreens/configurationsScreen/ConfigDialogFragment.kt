package elite.kit.outwait.recyclerviewScreens.configurationsScreen

import android.app.AlertDialog
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
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.databinding.ConfigDialogFragmentBinding
import elite.kit.outwait.recyclerviewScreens.managmentViewScreen.ManagmentViewFragment
import mobi.upod.timedurationpicker.TimeDurationPicker
import org.joda.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@AndroidEntryPoint
class ConfigDialogFragment : Fragment() {

    private lateinit var binding: ConfigDialogFragmentBinding
    private val viewModel: ConfigDialogViewModel by viewModels()
    private lateinit var builder: AlertDialog.Builder
    private lateinit var displayingDialog: AlertDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.config_dialog_fragment, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner=viewLifecycleOwner

        //Setup format
        setUpFormat()

        builder = AlertDialog.Builder(activity)
        builder.apply {
            setView(R.layout.full_screen_progress_bar)
            setTitle(getString(R.string.process_title))
            setCancelable(true)
        }
        displayingDialog= builder.create()

        //TODO check queue empty to switch mode
        //get default values from sever
        viewModel.repo.getObservablePreferences().observe(viewLifecycleOwner, Observer<Preferences> {
            viewModel.standardSlotDauer.value=it.defaultSlotDuration
            viewModel.delayNotificationTime.value=it.delayNotificationTime
            viewModel.notificationTime.value=it.notificationTime
            viewModel.prioritizationTime.value=it.prioritizationTime
            viewModel.isModusTwo.value=it.mode== Mode.TWO

            binding.durationStandardSlot.duration=it.defaultSlotDuration.millis
            binding.durationDelay.duration=it.delayNotificationTime.millis
            binding.durationPrioritization.duration=it.prioritizationTime.millis
            binding.durationNotification.duration=it.notificationTime.millis
            binding.sMode.isChecked= it.mode==Mode.TWO

            Log.i("viewModel","${viewModel.standardSlotDauer.value}")
            Log.i("observation","trueeeeeeeeee")
            displayingDialog.cancel()
        })


        //Setup Dialog
//        displayValues()

        //TODO check problems to late pass data
        //pass new default values from user to server
        binding.btnSave.setOnClickListener {
            viewModel.standardSlotDauer.value= Duration(binding.durationStandardSlot.duration)
            viewModel.delayNotificationTime.value=Duration(binding.durationDelay.duration)
            viewModel.notificationTime.value=Duration(binding.durationNotification.duration)
            viewModel.prioritizationTime.value=Duration(binding.durationPrioritization.duration)
            viewModel.isModusTwo.value=binding.sMode.isChecked
            viewModel.saveConfigValues()
            displayingDialog.show()
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

    }


}
