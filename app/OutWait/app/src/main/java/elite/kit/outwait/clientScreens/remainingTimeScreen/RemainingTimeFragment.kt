package elite.kit.outwait.clientScreens.remainingTimeScreen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.SharedElementCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.databinding.RemainingTimeFragmentBinding


@AndroidEntryPoint
class RemainingTimeFragment : Fragment() {

    private val viewModel: RemainingTimeViewModel by viewModels()
    private lateinit var binding: RemainingTimeFragmentBinding
    private var isFirstBackPressed = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding=DataBindingUtil.inflate(inflater,
            R.layout.remaining_time_fragment,
            container,
            false)
        binding.viewModel=this.viewModel
        exitApp()
        return binding.root
    }

    private fun exitApp() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {
                    if (childFragmentManager.backStackEntryCount !== 0) {
                    } else {
                        if (isFirstBackPressed) {
                        } else {
                            Log.i("back button", "back button double pressed")

                            isFirstBackPressed = true
                            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_LONG).show()
                            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                                isFirstBackPressed = false
                            }, 1500)
                        }
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }







}
