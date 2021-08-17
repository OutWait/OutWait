package edu.kit.outwait.loginSystem

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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import dagger.hilt.android.AndroidEntryPoint
import edu.kit.outwait.R
import edu.kit.outwait.clientDatabase.ClientInfo
import edu.kit.outwait.databinding.LoginFragmentBinding
import edu.kit.outwait.managmentLogin.institutLoginScreen.InstitutLoginViewModel
import edu.kit.outwait.qrCode.scanner.CaptureAct
import kotlinx.android.synthetic.main.login_fragment.*

/**
 * Represents the login screen for client and management
 *
 */
private const val CAMERA_RQ = 102
private const val CAMERA_NAME = "camera"

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private lateinit var binding: LoginFragmentBinding
    private val viewModel: UserViewModel by activityViewModels()

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
        binding = DataBindingUtil.inflate(inflater, R.layout.login_fragment, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.ivScan.setOnClickListener {
            checkForPermission(android.Manifest.permission.CAMERA, CAMERA_NAME, CAMERA_RQ)
        }
        val navController = findNavController()

        viewModel.loginResponse.observe(viewLifecycleOwner) { listOfUsers ->
            when {
                listOfUsers.isEmpty() -> {
                }
                listOfUsers.component1() == false -> {
                }
                listOfUsers.component1() == true -> {
                    navController.popBackStack()
                }
                listOfUsers.component1() is ClientInfo -> {
                    navController.popBackStack()
                }
            }
        }

        viewModel.loginData.observe(viewLifecycleOwner, Observer {
            if (!(it.first==""&&it.second=="")) {
                viewModel.instituteName.value = it.first
                viewModel.institutePassword.value = it.second
            } else {
                viewModel.instituteName.value = ""
                viewModel.institutePassword.value = ""
            }
        })



        binding.etSlotCode.setBackgroundResource(R.drawable.shape_code_edit_text)

        binding.etSlotCode.setOnCodeChangedListener { (code, completed) ->
            if (completed) {
                viewModel.clientSlotCode.value = code
                viewModel.enterSlotCode()
                Log.i("slotCode", "${viewModel.clientSlotCode.value}")
            }
        }

        return binding.root
    }

    /**
     * Validates whether user has allowed to scan with his camera
     *
     * @param permission Type of permission
     * @param name Name of permission
     * @param requestCode Identifier of permission
     */
    private fun checkForPermission(permission: String, name: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ActivityCompat.checkSelfPermission(
                    requireActivity().applicationContext,
                    permission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(
                        requireActivity().applicationContext,
                        "$name ${getString(R.string.permission_allowed)}",
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

    /**
     * Callback for the result from requesting permissions. This method is invoked for every call on requestPermissions()
     *
     * @param requestCode Identifier of request
     * @param permissions Permissions
     * @param grantResults Result whether denied or allowed
     */
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
            scanCode()
        }
    }

    /**
     * Displays a dialog which explains reason for using the camera
     *
     * @param permission Type of permission
     * @param name Name of permission
     * @param requestCode Identifier of request
     */
    private fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(requireActivity())

        builder.apply {
            setMessage(getString(R.string.explanation_text_permission))
            setTitle(getString(R.string.title_permission_dialog))
            setPositiveButton(getString(R.string.ok)) { dialog, which ->
                requestPermissions(
                    arrayOf(permission),
                    requestCode
                )
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    /**
     * Starts to open a new activity to scan with the camera
     *
     */
    private fun scanCode() {
        val integrator: IntentIntegrator = IntentIntegrator.forSupportFragment(this@LoginFragment)
        integrator.captureActivity = CaptureAct::class.java
        integrator.setOrientationLocked(true)
        integrator.setBeepEnabled(true)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt(getString(R.string.text_scanning))
        integrator.initiateScan()
    }

    /**
     * Returns dedicated scanned qr code
     *
     * @param requestCode Identifier of request
     * @param resultCode Integer result code returned by the child activity
     * @param data Returned data from scan
     */
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
                viewModel.clientSlotCode.value = result.contents
                viewModel.enterSlotCode()
            } else {
                Toast.makeText(
                    requireActivity().applicationContext,
                    getString(R.string.no_result),
                    Toast.LENGTH_LONG
                ).show()
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


}
