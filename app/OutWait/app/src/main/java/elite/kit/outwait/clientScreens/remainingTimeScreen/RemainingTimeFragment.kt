package elite.kit.outwait.clientScreens.remainingTimeScreen

import android.os.Bundle
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
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.databinding.RemainingTimeFragmentBinding


@AndroidEntryPoint
class RemainingTimeFragment : Fragment() {

    private val viewModel: RemainingTimeViewModel by viewModels()
    private lateinit var binding: RemainingTimeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding=DataBindingUtil.inflate(inflater,
            R.layout.remaining_time_fragment,
            container,
            false)
        binding.viewModel=this.viewModel




        return binding.root
    }

    fun onBackPressed() {

        Toast.makeText(context, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

    }




    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

}
