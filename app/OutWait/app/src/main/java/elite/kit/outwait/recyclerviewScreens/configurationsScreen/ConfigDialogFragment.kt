package elite.kit.outwait.recyclerviewScreens.configurationsScreen

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import elite.kit.outwait.R
import elite.kit.outwait.databinding.ConfigDialogFragmentBinding
import mobi.upod.timedurationpicker.TimeDurationPicker

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

        //Setup Dialog
        displaySetValues()
        //Setup format
        setUpFormat()
        return binding.root
    }

    private fun setUpFormat() {
        binding.durationDelay.setTimeUnits(TimeDurationPicker.HH_MM)
        binding.durationNotification.setTimeUnits(TimeDurationPicker.HH_MM)
        binding.durationPrioritization.setTimeUnits(TimeDurationPicker.HH_MM)
        binding.durationStandardSlot.setTimeUnits(TimeDurationPicker.HH_MM)
    }

    private fun displaySetValues() {
        TODO("Not yet implemented")
    }


}
