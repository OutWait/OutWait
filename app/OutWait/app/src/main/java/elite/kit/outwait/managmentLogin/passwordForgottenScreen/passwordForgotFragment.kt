package elite.kit.outwait.managmentLogin.passwordForgottenScreen

import android.os.Bundle
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
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.databinding.PasswordForgotFragmentBinding


@AndroidEntryPoint
class passwordForgotFragment : Fragment() {



    private val viewModel: PasswordForgotViewModel by viewModels()
    private lateinit var binding:PasswordForgotFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.password_forgot_fragment,
            container,
            false)
        binding.viewModel=this.viewModel
        binding.lifecycleOwner=this

        return binding.root
    }




}
