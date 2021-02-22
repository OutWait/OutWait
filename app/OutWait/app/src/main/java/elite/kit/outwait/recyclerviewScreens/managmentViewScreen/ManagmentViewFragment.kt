package elite.kit.outwait.recyclerviewScreens.managmentViewScreen

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.databinding.AddSlotDialogFragmentBinding
import elite.kit.outwait.databinding.ManagmentViewFragmentBinding
import elite.kit.outwait.recyclerviewScreens.editSlotDialog.EditTimeSlotDialogFragment
import elite.kit.outwait.recyclerviewScreens.slotDetailDialog.SlotDetailDialogFragment
import elite.kit.outwait.recyclerviewSetUp.functionality.SlotAdapter
import elite.kit.outwait.recyclerviewSetUp.functionality.SlotItemTouchHelper
import elite.kit.outwait.waitingQueue.timeSlotModel.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.*
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

@AndroidEntryPoint
class ManagmentViewFragment : Fragment(), ItemActionListener {

    private val viewModel: ManagmentViewViewModel by viewModels()
    private lateinit var binding: ManagmentViewFragmentBinding
    private lateinit var slotAdapter: SlotAdapter
    private lateinit var builder: AlertDialog.Builder


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.managment_view_fragment, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this

        //RecyclerView SetUp
        binding.slotList.layoutManager = LinearLayoutManager(activity)
        binding.slotList.setHasFixedSize(true)

        builder = AlertDialog.Builder(activity)
        builder.apply {
            setView(R.layout.full_screen_progress_bar)
            setTitle(getString(R.string.process_title))
            setCancelable(true)
        }
        builder.create()
        builder.setCancelable(true)

        viewModel.slotQueue.observe(viewLifecycleOwner) { list ->
            slotAdapter.updateSlots(list.toMutableList())
            //TODO dismiss progress bar dialog
        }

        //Add listener for recyclerview
        slotAdapter = SlotAdapter(mutableListOf<TimeSlot>(), this)
        var callback: ItemTouchHelper.Callback = SlotItemTouchHelper(slotAdapter)
        var itemTouchHelper: ItemTouchHelper = ItemTouchHelper(callback)
        slotAdapter.setTouchHelper(itemTouchHelper)
        itemTouchHelper.attachToRecyclerView(binding.slotList)
        binding.slotList.adapter = slotAdapter

        //Add action bar icon
        setHasOptionsMenu(true)

        exitApp()

        return binding.root
    }


   /* private fun fakeSlotList(): MutableList<TimeSlot> {
        var slotList = mutableListOf<TimeSlot>()


        var start = DateTime(DateTime.now()).plusHours(1)
        var end = start.plusMinutes(33)
        *//* Log.i("datetime", "${TransformationInput.formatDateTime(20,15)}")
         Log.i("duration", "${TransformationInput.formatDuration(6000)}")
         Log.i("interval", "${TransformationInput.formatInterval(6000)}")
         Log.i("interval", "${Duration(200L).toIntervalFrom(DateTime.now())}")*//*

        for (i in 1..1) {

            slotList!!.add(FixedTimeSlot(Interval(start, end),
                "4444",
                "Müller",
                DateTime(DateTime.now().year,
                    DateTime.now().monthOfYear, DateTime.now().dayOfWeek, 22, 33)))
        }

        for (i in 1..3) {
            slotList!!.add(Pause(Interval(start, end)))
        }

        for (i in 1..1) {
            slotList!!.add(FixedTimeSlot(Interval(start, end),
                "111",
                "Hans",
                DateTime(DateTime.now().year,
                    DateTime.now().monthOfYear,
                    DateTime.now().dayOfWeek,
                    22,
                    33)))
        }

        for (i in 1..3) {
            slotList!!.add(SpontaneousTimeSlot(Interval(start, end), "2222", "Frank"))
        }
        return slotList
    }*/

    override fun onItemClicked(position: Int) {
        var detailDialog =
            SlotDetailDialogFragment(slotAdapter.slotList[position] as ClientTimeSlot)
        detailDialog.show(childFragmentManager, "ssss")
    }


    override  fun onItemSwiped(position: Int, removedSlot: TimeSlot) {
        var resetDelete =
            Snackbar.make(binding.slotList, "${getIdentifier(removedSlot)}", Snackbar.LENGTH_LONG)
                .setAction(getString(
                    R.string.undo)) {
                    slotAdapter.slotList.add(position, removedSlot)
                    slotAdapter.notifyItemInserted(position)
                    slotAdapter.notifyItemRangeChanged(0, slotAdapter.slotList.size - 1)
                    builder.show()
                }
        resetDelete.show()

//        viewModel.notifyDelteSlot()
//        viewModel.notifyEndCurrentSlot()
    }

    private fun getIdentifier(slot: TimeSlot): String {
        //Guarantee slot is only fixed or spo by GUI
        return (slot as ClientTimeSlot).auxiliaryIdentifier
    }

    override fun editTimeSlot(position: Int) {
        var editDialog =
            EditTimeSlotDialogFragment(slotAdapter.slotList[position] as ClientTimeSlot)
        editDialog.show(childFragmentManager, "aaa")
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.overflow, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.navigateToConfigDialog()
        return true
    }

    private fun exitApp() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {


                        Toast.makeText(context, "Please only possibility to logout", Toast.LENGTH_LONG)
                            .show()

                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

}
