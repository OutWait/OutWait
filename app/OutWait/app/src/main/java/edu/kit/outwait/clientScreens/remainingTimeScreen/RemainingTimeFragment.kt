package edu.kit.outwait.clientScreens.remainingTimeScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import edu.kit.outwait.R
import edu.kit.outwait.databinding.RemainingTimeFragmentBinding

/**
 * Represents the screen that shows the waiting time to the client
 *
 */
@AndroidEntryPoint
class RemainingTimeFragment : Fragment() {

    private val viewModel: RemainingTimeViewModel by viewModels()
    private lateinit var binding: RemainingTimeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.remaining_time_fragment,
            container,
            false
        )
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this

        viewModel.remainingTime.observe(viewLifecycleOwner, Observer {
            if (it == "?"){
                binding.btn.text = getText(R.string.try_refresh)
            } else {
                binding.btn.text = it
            }
        })

        viewModel.clientInfoList.observe(viewLifecycleOwner, Observer {
            if(it.isEmpty()){
                findNavController().popBackStack()
            }
        })

        exitApp()

        return binding.root
    }

    /**
     * does not let the client leave the screen until his appointment is finished
     *
     */
    private fun exitApp() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    Toast.makeText(
                        context,
                        getString(R.string.back_press_client),
                        Toast.LENGTH_LONG
                    )
                        .show()

                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
}
