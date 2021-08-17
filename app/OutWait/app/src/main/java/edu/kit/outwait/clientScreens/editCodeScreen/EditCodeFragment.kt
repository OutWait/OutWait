package edu.kit.outwait.clientScreens.editCodeScreen

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import dagger.hilt.android.AndroidEntryPoint
import edu.kit.outwait.R
import edu.kit.outwait.clientDatabase.ClientInfo
import edu.kit.outwait.clientScreens.remainingTimeScreen.RemainingTimeViewModel
import edu.kit.outwait.databinding.EditCodeFragmentBinding
import edu.kit.outwait.qrCode.scanner.CaptureAct

@AndroidEntryPoint
class EditCodeFragment : Fragment() {

    private  val viewModel: EditCodeViewModel by viewModels()
    private lateinit var binding: EditCodeFragmentBinding
    val CAMERA_RQ = 102



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.edit_code_fragment, container, false)
        binding.viewModel=this.viewModel
        binding.lifecycleOwner=this

        binding.ibStartScan.setOnClickListener {
            checkForPermission(android.Manifest.permission.CAMERA, "camera", CAMERA_RQ)
        }

        viewModel.loginResponse.observe(viewLifecycleOwner){
            if(it.isEmpty()){
                Toast.makeText(context,"Failed",Toast.LENGTH_SHORT)
            }else if(it.component1() is ClientInfo) {
                Log.i("forward back","${it.component1().toString()}")
                Navigation.findNavController(binding.root).navigate(R.id.remainingTimeFragment)
//                val response= findNavController().popBackStack()
//                Log.i("ss","$response")
            }
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
        val integrator: IntentIntegrator = IntentIntegrator.forSupportFragment(this@EditCodeFragment)
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
                viewModel.enterSlotCode(result.contents)
            } else {
                Toast.makeText(requireActivity().applicationContext, "No Result", Toast.LENGTH_LONG).show()
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }




}
