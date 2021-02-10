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
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.databinding.RemainingTimeFragmentBinding


@AndroidEntryPoint
class RemainingTimeFragment : Fragment() {

    private val viewModel: RemainingTimeViewModel by viewModels()
    private lateinit var binding: RemainingTimeFragmentBinding
    private var isFirstBackPressed = false
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
        binding.lifecycleOwner=this


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
            startAngle = MINIMUM_INTERVAL
            progressDirection = CircularProgressBar.ProgressDirection.TO_LEFT
        }

        counterDownTimer()

        return binding.root
    }

    private fun counterDownTimer() {
        //TODO set remainingTime
        counterDownTimer = object : CountDownTimer(10000 + CORRECTION_TIME, COUNTDOWN_INTERVAL) {

            override fun onTick(millisUntilFinished: Long) {
                binding.circularProgressBar.setProgressWithAnimation(millisUntilFinished.toFloat() - CORRECTION_TIME)
                binding.tvRemainingTime.text =
                    ((millisUntilFinished / COUNTDOWN_INTERVAL).toString())
            }

            override fun onFinish() {
                binding.tvRemainingTime.text = "Please go to your institut"
                binding.circularProgressBar.progress = MINIMUM_INTERVAL
            }
        }.start()

    }

}
