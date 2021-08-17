package edu.kit.outwait.managmentLogin.passwordForgottenScreen

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
import edu.kit.outwait.R
import edu.kit.outwait.databinding.PasswordForgotFragmentBinding

/**
 * Represents the screen to reset its password from management
 *
 */
@AndroidEntryPoint
class PasswordForgotFragment : Fragment() {



    private val viewModel: PasswordForgotViewModel by viewModels()
    private lateinit var binding:PasswordForgotFragmentBinding

    /**
     * Creates view of this fragment
     *
     * @param inflater Instantiates a layout XML file into its corresponding View objects.
     * @param container Includes all view of this fragment
     * @param savedInstanceState A mapping from String keys to various Parcelable values.
     * @return Instantiates a layout XML file into its corresponding View objects.
     */
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
