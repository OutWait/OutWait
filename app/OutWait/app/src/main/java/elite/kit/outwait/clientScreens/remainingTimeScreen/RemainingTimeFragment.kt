package elite.kit.outwait.clientScreens.remainingTimeScreen

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import elite.kit.outwait.R
import elite.kit.outwait.clientScreens.editCodeScreen.EditCodeViewModel
import elite.kit.outwait.databinding.EditCodeFragmentBinding
import elite.kit.outwait.databinding.RemainingTimeFragmentBinding

class RemainingTimeFragment : Fragment() {

    private val viewModel: RemainingTimeViewModel by viewModels()
    private lateinit var binding: RemainingTimeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding=DataBindingUtil.inflate(inflater,R.layout.remaining_time_fragment, container, false)
        binding.viewModel=this.viewModel
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

}
