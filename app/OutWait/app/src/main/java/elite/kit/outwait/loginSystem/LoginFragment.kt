package elite.kit.outwait.loginSystem

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import elite.kit.outwait.R
import elite.kit.outwait.clientDatabase.ClientInfo
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
        val navController = findNavController()


        viewModel.loginResponse.observe(viewLifecycleOwner){listOfUsers->
            when {
                listOfUsers.isEmpty() -> {
                    Toast.makeText(context,"FAILED",Toast.LENGTH_LONG)
                }
                listOfUsers.component1() == false -> {
                    //TODO failed login should override loggedIn with false
                    Toast.makeText(context,"FAILED",Toast.LENGTH_LONG)
                }
                listOfUsers.component1() == true -> {
                    navController.popBackStack()
                }
                listOfUsers.component1() is ClientInfo -> {
                    navController.popBackStack()
                }
            }
        }
        return binding.root
    }


}
