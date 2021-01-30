package elite.kit.outwait.managmentLogin.institutLoginScreen

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import elite.kit.outwait.R
import elite.kit.outwait.databinding.InstitutLoginFragmentBinding

class InstitutLoginFragment : Fragment() {

    companion object {
        fun newInstance() = InstitutLoginFragment()
    }

    private lateinit var viewModel: InstitutLoginViewModel
    private lateinit var binding: InstitutLoginFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(this).get(InstitutLoginViewModel::class.java)
        binding = DataBindingUtil.inflate(inflater, R.layout.institut_login_fragment, container, false)
        return binding.root
    }

}
