package elite.kit.outwait.recyclerviewScreens.managmentViewScreen

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import elite.kit.outwait.R
import elite.kit.outwait.databinding.InstitutLoginFragmentBinding
import elite.kit.outwait.databinding.ManagmentViewFragmentBinding

class managmentViewFragment : Fragment() {


    private lateinit var viewModel: ManagmentViewViewModel
    private lateinit var binding: ManagmentViewFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ManagmentViewViewModel::class.java)
        binding =DataBindingUtil.inflate(inflater,R.layout.managment_view_fragment, container, false)
        binding.viewModel=this.viewModel
        return binding.root
    }
}
