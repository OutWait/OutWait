package elite.kit.outwait.loginSystem

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import elite.kit.outwait.R
import elite.kit.outwait.databinding.FragmentLoginBinding
import elite.kit.outwait.managmentLogin.institutLoginScreen.InstitutLoginViewModel


class LoginFragment : Fragment() {


    private lateinit var binding:FragmentLoginBinding
    private val viewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_login, container, false)
        binding.viewModel=this.viewModel
        binding.lifecycleOwner=viewLifecycleOwner

        return binding.root

    }


}
