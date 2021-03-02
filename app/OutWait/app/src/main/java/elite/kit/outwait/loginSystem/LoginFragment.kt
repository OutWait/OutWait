package elite.kit.outwait.loginSystem

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.clientDatabase.ClientInfo
import elite.kit.outwait.databinding.LoginFragmentBinding
import elite.kit.outwait.managmentLogin.institutLoginScreen.InstitutLoginViewModel
import elite.kit.outwait.qrCode.scanner.CaptureAct
import kotlinx.android.synthetic.main.login_fragment.*

@AndroidEntryPoint
class LoginFragment : Fragment() {


    private lateinit var binding:LoginFragmentBinding
    private val viewModel: UserViewModel by activityViewModels()
    val CAMERA_RQ = 102

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding=DataBindingUtil.inflate(inflater,R.layout.login_fragment, container, false)
        binding.viewModel=this.viewModel
        binding.lifecycleOwner=viewLifecycleOwner
        val navController = findNavController()

        binding.ivScan.setOnClickListener {
            checkForPermission(android.Manifest.permission.CAMERA, "camera", CAMERA_RQ)
        }

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

        binding.etInstituteName.setOnFocusChangeListener { v, hasFocus ->
            if(viewModel.instituteName.value!!.isNotBlank() && !hasFocus){
                binding.boxesInstituteName.secondaryColor=R.color.code_edit_text_bottom_line
            }else{
                binding.boxesInstituteName.secondaryColor=R.color.text_missing

            }
        }
        binding.etInstitutePassword.setOnFocusChangeListener { v, hasFocus ->
            if(viewModel.institutePassword.value!!.isNotBlank() && !hasFocus){
                binding.boxesInstitutePassword.secondaryColor=R.color.code_edit_text_bottom_line
            }else{
                binding.boxesInstitutePassword.secondaryColor=R.color.text_missing
            }
        }
       binding.etSlotCode.setBackgroundResource(R.drawable.shape_code_edit_text)

        binding.etSlotCode.setOnCodeChangedListener { (code, completed) ->
            if (completed) {
                viewModel.clientSlotCode.value= code
                viewModel.enterSlotCode()
                Log.i("slotCode","${viewModel.clientSlotCode.value}")            }
        }



        return binding.root
    }

    private fun checkForPermission(permission: String, name: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ActivityCompat.checkSelfPermission(
                    requireActivity().applicationContext,
                    permission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(
                        requireActivity().applicationContext,
                        "$name permisson allowed",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    scanCode()
                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(
                    permission,
                    name,
                    requestCode
                )
                else -> requestPermissions(
                    arrayOf(permission),
                    requestCode
                )

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                requireActivity().applicationContext,
                "camera permission refused",
                Toast.LENGTH_LONG
            )
                .show()
        } else {
            Toast.makeText(
                requireActivity().applicationContext,
                "camera permission granted",
                Toast.LENGTH_LONG
            )
                .show()
            //TODO test below line
            scanCode()
        }
    }

    private fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(requireActivity())

        builder.apply {
            setMessage("Permission to access your $name is required to scan your qr scan")
            setTitle("Permission required")
            setPositiveButton("OK") { dialog, which ->
                requestPermissions(
                    arrayOf(permission),
                    requestCode
                )
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun scanCode() {
        val integrator: IntentIntegrator = IntentIntegrator.forSupportFragment(this@LoginFragment)
        integrator.captureActivity = CaptureAct::class.java
        integrator.setOrientationLocked(true)
        integrator.setBeepEnabled(true)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scanning Code")
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var result: IntentResult =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "${result.contents}",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.clientSlotCode.value=result.contents
                viewModel.enterSlotCode()
            } else {
                Toast.makeText(requireActivity().applicationContext, "No Result", Toast.LENGTH_LONG).show()
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


}
