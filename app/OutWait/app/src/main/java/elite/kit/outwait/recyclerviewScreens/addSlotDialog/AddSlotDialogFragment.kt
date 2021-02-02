package elite.kit.outwait.recyclerviewScreens.addSlotDialog

import android.app.AlertDialog
import android.app.Dialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.databinding.DataBindingUtil
import elite.kit.outwait.R
import elite.kit.outwait.databinding.AddSlotDialogFragmentBinding

class AddSlotDialogFragment : AppCompatDialogFragment() {

    companion object {
        fun newInstance() = AddSlotDialogFragment()
    }

    private lateinit var viewModel: AddSlotDialogViewModel
    private lateinit var binding: AddSlotDialogFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        viewModel = ViewModelProvider(this).get(AddSlotDialogViewModel::class.java)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.add_slot_dialog_fragment, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.add_slot_dialog_fragment, null)

        //TODO for appointmenttime and duration take a sample
        //TODO use binding in layout, confirm only calls method from vm

        builder.apply {
            setView(view)
            setTitle(getString(R.string.title_add_slot))
            setPositiveButton(getString(R.string.confirm)) { dialog, which ->


            }
            setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.cancel()
            }

        }
        return builder.create()

    }
}
