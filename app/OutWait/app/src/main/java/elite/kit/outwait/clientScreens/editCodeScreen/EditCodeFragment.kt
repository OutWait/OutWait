package elite.kit.outwait.clientScreens.editCodeScreen

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.databinding.EditCodeFragmentBinding
import elite.kit.outwait.databinding.InstitutLoginFragmentBinding
import elite.kit.outwait.managmentLogin.institutLoginScreen.InstitutLoginViewModel

@AndroidEntryPoint
class EditCodeFragment : Fragment() {

    private val viewModel: EditCodeViewModel by viewModels()
    private lateinit var binding: EditCodeFragmentBinding



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.edit_code_fragment, container, false)
        binding.viewModel=this.viewModel
        return binding.root
    }



}
