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
import androidx.navigation.fragment.findNavController
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.databinding.RemainingTimeFragmentBinding


@AndroidEntryPoint
class RemainingTimeFragment : Fragment() {

    private val viewModel: RemainingTimeViewModel by viewModels()
    private lateinit var binding: RemainingTimeFragmentBinding
    private lateinit var counterDownTimer: CountDownTimer

    companion object {
        private const val CORRECTION_TIME = 1000L
        private const val COUNTDOWN_INTERVAL = 1000L
        private const val MINIMUM_INTERVAL = 1000f
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.remaining_time_fragment,
            container,
            false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this


        binding.circularProgressBar.apply {


            // Set Progress Max
            //TODO set remianing time
            progressMax = 10000f


            // Set ProgressBar Color
            progressBarColor = Color.BLACK
            // or with gradient
            progressBarColorStart = Color.GRAY
            progressBarColorEnd = Color.RED
            progressBarColorDirection = CircularProgressBar.GradientDirection.TOP_TO_BOTTOM

            // Set background ProgressBar Color
            backgroundProgressBarColor = Color.GRAY
            // or with gradient
            backgroundProgressBarColorStart = Color.WHITE
            backgroundProgressBarColorEnd = Color.RED
            backgroundProgressBarColorDirection =
                CircularProgressBar.GradientDirection.TOP_TO_BOTTOM

            // Set Width
            progressBarWidth = 7f // in DP
            backgroundProgressBarWidth = 3f // in DP

            // Other
            roundBorder = true
            startAngle = 0F
            progressDirection = CircularProgressBar.ProgressDirection.TO_LEFT
        }

        counterDownTimer()
        exitApp()

        return binding.root
    }

    private fun counterDownTimer() {
        //TODO set remainingTime
        counterDownTimer = object : CountDownTimer(10000L + CORRECTION_TIME, COUNTDOWN_INTERVAL) {

            override fun onTick(millisUntilFinished: Long) {
                binding.circularProgressBar.setProgressWithAnimation(millisUntilFinished.toFloat() - CORRECTION_TIME)
                binding.tvRemainingTime.text =
                    ((millisUntilFinished / 1000L).toString())
            }

            override fun onFinish() {
                binding.tvRemainingTime.text = "Please go to your institute"
                binding.circularProgressBar.progress = 0F
                Toast.makeText(context,
                    "Press back button to enter a new slot code",
                    Toast.LENGTH_LONG)
                    .show()
            }
        }.start()

    }

    private fun exitApp() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {


                    if (binding.circularProgressBar.progress == 0F
                    ) {
                        //TODO delete clientinfo object
                        Log.i("backButton", "go to loginfragment")


                        /*Handler(Looper.getMainLooper()).postDelayed(Runnable {
                            press = false
                        }, 1500)*/
                    } else {
                        Toast.makeText(context, "Click home button to exit", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

}
