package elite.kit.outwait.recyclerviewScreens.editFixedslotDialog

import android.app.AlertDialog
import android.app.Dialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.databinding.DataBindingUtil
import elite.kit.outwait.R
import elite.kit.outwait.databinding.AddSlotDialogFragmentBinding
import elite.kit.outwait.databinding.EditTimeSlotDialogFragmentBinding
import mobi.upod.timedurationpicker.TimeDurationPicker

class EditTimeSlotDialogFragment : AppCompatDialogFragment() {



    private lateinit var viewModel: EditTimeSlotDialogViewModel
    private lateinit var binding:EditTimeSlotDialogFragmentBinding


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = ViewModelProvider(this).get(EditTimeSlotDialogViewModel::class.java)
        binding = EditTimeSlotDialogFragmentBinding.inflate(LayoutInflater.from(context))
        binding.viewModel=this.viewModel
        binding.timeDurationInput.setTimeUnits(TimeDurationPicker.HH_MM)
        val builder = AlertDialog.Builder(activity)

        builder.apply {

            setView(binding.root)
            setTitle("Edit the slot")

            setPositiveButton(getString(R.string.confirm)) { dialog, which ->
                //TODO use TimeDate und Duration Format from benni
                //TODO identifier is not useable trough databinding PROBLEM ? binding.etIdentifierAddDialog.text
                //TODO same with isFixedSlot

            }

            setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.cancel()
            }

        }
        return builder.create()
    }
}
