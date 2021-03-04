package elite.kit.outwait.clientScreens.remainingTimeScreen

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.databinding.RemainingTimeFragmentBinding
import elite.kit.outwait.utils.TransformationOutput
import org.joda.time.Interval


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



        viewModel.clientInfoList.observe(viewLifecycleOwner, Observer {
            if(it.isEmpty()){
                findNavController().popBackStack()
            }
        })

        exitApp()

        return binding.root
    }


    private fun exitApp() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    Toast.makeText(
                        context,
                        "You can enter a new appointment after your appointment is finished",
                        Toast.LENGTH_LONG
                    )
                        .show()

                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

}
