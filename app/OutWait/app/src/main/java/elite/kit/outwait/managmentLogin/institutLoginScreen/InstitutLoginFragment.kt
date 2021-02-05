package elite.kit.outwait.managmentLogin.institutLoginScreen

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.databinding.InstitutLoginFragmentBinding
import elite.kit.outwait.recyclerviewScreens.addSlotDialog.AddSlotDialogFragment

@AndroidEntryPoint
class InstitutLoginFragment : Fragment(){

    companion object {
        fun newInstance() = InstitutLoginFragment()
    }

    private lateinit var viewModel: InstitutLoginViewModel
    private lateinit var binding: InstitutLoginFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(this).get(InstitutLoginViewModel::class.java)
        binding = DataBindingUtil.inflate(inflater, R.layout.institut_login_fragment, container, false)
        binding.viewModel=this.viewModel

//        navigate to ForgottenPasswordFragment
        binding.tvPasswordForgotten.setOnClickListener { view->
            view.findNavController().navigate(R.id.action_institutLoginFragment_to_passwordForgotFragment)
        }
        //navigate to managementViewFragment
        binding.btnLogin.setOnClickListener{
           /* var add = AddSlotDialogFragment()
            add.show(requireActivity().supportFragmentManager, "missiles")*/
            viewModel.loginTried()
            it.findNavController().navigate(R.id.action_institutLoginFragment_to_managmentViewFragment)
        }
        return binding.root
    }

}
