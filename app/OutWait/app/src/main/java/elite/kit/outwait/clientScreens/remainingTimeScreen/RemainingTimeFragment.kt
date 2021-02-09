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
        counterDownTimer()

        binding.circularProgressBar.apply {
            // Set Progress
           // progress = 0f
            // or with animation
            //setProgressWithAnimation(20f, 1000) // =1s

            // Set Progress Max
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
            backgroundProgressBarColorDirection = CircularProgressBar.GradientDirection.TOP_TO_BOTTOM

            // Set Width
            progressBarWidth = 7f // in DP
            backgroundProgressBarWidth = 3f // in DP

            // Other
            roundBorder = true
            startAngle = 0f
            progressDirection = CircularProgressBar.ProgressDirection.TO_LEFT
        }



        return binding.root
    }

    private fun counterDownTimer(){
        counterDownTimer= object :CountDownTimer(11000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                binding.circularProgressBar.setProgressWithAnimation(millisUntilFinished.toFloat()-1000f)
                binding.tvRemainingTime.setText((millisUntilFinished / 1000).toString())
            }

            override fun onFinish() {
                binding.tvRemainingTime.setText("done!")
                binding.circularProgressBar.progress=0f
            }
        }.start()

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
