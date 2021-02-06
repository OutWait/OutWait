package elite.kit.outwait.managmentLogin.passwordForgottenScreen

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.databinding.PasswordForgotFragmentBinding

@AndroidEntryPoint
class passwordForgotFragment : Fragment() {

    companion object {
        fun newInstance() = passwordForgotFragment()
    }

    private lateinit var viewModel: PasswordForgotViewModel
    private lateinit var binding:PasswordForgotFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(PasswordForgotViewModel::class.java)
        binding = DataBindingUtil.inflate(inflater,R.layout.password_forgot_fragment, container, false)
        binding.viewModel=this.viewModel
        return binding.root
    }



}
