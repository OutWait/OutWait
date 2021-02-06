package elite.kit.outwait.recyclerviewScreens.addSlotDialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import elite.kit.outwait.R
import elite.kit.outwait.databinding.AddSlotDialogFragmentBinding
import mobi.upod.timedurationpicker.TimeDurationPicker
import org.joda.time.Duration
import org.joda.time.DateTime

class AddSlotDialogFragment : AppCompatDialogFragment() {

    private  val viewModel: AddSlotDialogViewModel by viewModels()
    private lateinit var binding: AddSlotDialogFragmentBinding



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        binding = AddSlotDialogFragmentBinding.inflate(LayoutInflater.from(context))



        binding.viewModel = this.viewModel
        binding.tpAppointmentTime.setIs24HourView(true)
      //  binding.timeDurationInput.setTimeUnits(TimeDurationPicker.HH_MM)

        //TODO check alternative timepicker for android 21 lolipop (hour)
        //TODO FixedSlot only possible in Modus 2 - binding.cbIsFixedSlot.isEnabled & appointment timepicker disable

        builder.apply {

            setView(binding.root)
            setTitle(getString(R.string.title_add_slot))

            setPositiveButton(getString(R.string.confirm)) { dialog, which ->
                //TODO use TimeDate und Duration Format from benni
                //TODO identifier is not useable trough databinding PROBLEM ? binding.etIdentifierAddDialog.text
                //TODO same with isFixedSlot
               var hour: Int = binding.tpAppointmentTime.currentHour
                Log.i("timepicker","$hour")
               // viewModel.notifyAddSlot(binding.timeDurationInput.duration / (3.6 * Math.pow(10.0,
               //     6.0)), 2000L)
            }

            setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.cancel()
            }

        }
        return builder.create()

    }
}
