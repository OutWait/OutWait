package edu.kit.outwait.managmentLogin.institutLoginScreen

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import edu.kit.outwait.R
import edu.kit.outwait.databinding.InstitutLoginFragmentBinding
import edu.kit.outwait.recyclerviewScreens.addSlotDialog.AddSlotDialogFragment

@AndroidEntryPoint
class InstitutLoginFragment : Fragment(){


    private val viewModel: InstitutLoginViewModel by viewModels()
    private lateinit var binding: InstitutLoginFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.institut_login_fragment, container, false)
        binding.viewModel=this.viewModel
        return binding.apply { lifecycleOwner=this@InstitutLoginFragment }.root
    }

}
