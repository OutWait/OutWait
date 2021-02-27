package elite.kit.outwait.recyclerviewScreens.managementViewScreen

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.Callback
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.databinding.ManagementViewFragmentBinding
import elite.kit.outwait.recyclerviewScreens.editSlotDialog.EditTimeSlotDialogFragment
import elite.kit.outwait.recyclerviewScreens.slotDetailDialog.SlotDetailDialogFragment
import elite.kit.outwait.recyclerviewSetUp.functionality.SlotAdapter
import elite.kit.outwait.recyclerviewSetUp.functionality.SlotItemTouchHelper
import elite.kit.outwait.recyclerviewSetUp.viewHolder.HeaderTransaction
import elite.kit.outwait.waitingQueue.timeSlotModel.*
import kotlinx.android.synthetic.main.full_screen_progress_bar.*
import kotlinx.android.synthetic.main.management_view_fragment.*
import org.joda.time.*

@AndroidEntryPoint
class ManagementViewFragment : Fragment(), ItemActionListener {
    private val viewModel: ManagmentViewViewModel by viewModels()

    companion object{
        lateinit var displayingDialog: AlertDialog
        var movementInfo = MutableLiveData(mutableListOf<String>())
        fun ss() {}
    }

    private lateinit var binding: ManagementViewFragmentBinding
    private lateinit var slotAdapter: SlotAdapter
    private lateinit var builder:AlertDialog.Builder
    private var CURREND_SLOT1=0
    private var CURREND_SLOT2=1
    private var FIRST_POSITION=0

    private var deleteHeader=false



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.management_view_fragment, container, false)
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
        displayingDialog=builder.create()

        var start = DateTime(DateTime.now()).plusHours(1)
        var end = start.plusMinutes(33)

        //TODO check except
        viewModel.repo.getObservableTimeSlotList().observe(viewLifecycleOwner, Observer { list ->
            slotAdapter.updateSlots(list.toMutableList())
            var ss: MutableList<DataItem> = list.toMutableList()
                ss.add(FIRST_POSITION, HeaderItem())
            slotAdapter.updateSlots(ss)
            //TODO dismiss progress bar dialog
            displayingDialog.dismiss()
        })

        movementInfo.observe(viewLifecycleOwner){
            Log.i("movement","notified")
            displayingDialog.dismiss()
//            viewModel.moveSlotAfterAnother(it.first(),it.last())
        }

        viewModel.isInTransaction.observe(viewLifecycleOwner){
            if(it){
                //TODO easy way with layout above recyclerview layout
//               slotAdapter.updateSlots(slotAdapter.slotList.add(FIRST_POSITION, HeaderItem(Interval(200L))))
            }
            //TODO maybe dismiss dialog during to abort
        }


//        viewModel.weatherLocations.observe(viewLifecycleOwner) {
//            weatherOverviewAdapter.updateWeatherLocations(it)
//        }

        //Add listener for recyclerview
        slotAdapter = SlotAdapter( mutableListOf<DataItem>(), this)
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

     fun forwarderMove(movedSlot: String, otherSlot: String){
        viewModel.moveSlotAfterAnother(movedSlot, otherSlot)
    }

    private fun fakeSlotList(): MutableList<TimeSlot> {
        var slotList = mutableListOf<TimeSlot>()


        var start = DateTime(DateTime.now()).plusHours(1)
        var end = start.plusMinutes(33)
        /* Log.i("datetime", "${TransformationInput.formatDateTime(20,15)}")
         Log.i("duration", "${TransformationInput.formatDuration(6000)}")
         Log.i("interval", "${TransformationInput.formatInterval(6000)}")
         Log.i("interval", "${Duration(200L).toIntervalFrom(DateTime.now())}")*/

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
    }

    override fun onItemClicked(position: Int) {
        var detailDialog =
            SlotDetailDialogFragment(slotAdapter.slotList[position] as ClientTimeSlot)
        detailDialog.show(childFragmentManager, "ssss")
    }


    override fun onItemSwiped(position: Int, removedSlot: TimeSlot) {

        var resetDelete =
            Snackbar.make(binding.slotList, "${getIdentifier(removedSlot)}", Snackbar.LENGTH_LONG)
                .setAction(getString(
                    R.string.undo)) {
                    slotAdapter.slotList.add(position, removedSlot)
                    slotAdapter.notifyItemInserted(position)
                    slotAdapter.notifyItemRangeChanged(0, slotAdapter.slotList.size - 1)
                    displayingDialog.show()
                    displayingDialog.fullScreenProgressBar.indeterminateMode =true


                }
        resetDelete.addCallback(object : Callback() {
            override fun onDismissed(snackbar: Snackbar, event: Int) {
                super.onDismissed(snackbar, event)
                if (event == DISMISS_EVENT_TIMEOUT) {
                    notifyDeleteSlot(position,removedSlot)
                    displayingDialog.show()
                    displayingDialog.fullScreenProgressBar.indeterminateMode =true
                }
            }
        })
        resetDelete.show()
    }

    private fun notifyDeleteSlot(position: Int, removedSlot: TimeSlot) {
        var removedClientSlot = removedSlot as ClientTimeSlot
        when (position) {
            //TODO check delete slot first then after header slot (also first)
            CURREND_SLOT1, CURREND_SLOT2 -> viewModel.endCurrendSlot()
            else -> viewModel.deleteSlot(removedClientSlot.slotCode)
        }
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

        override fun saveTransaction() {
            Log.i("save","call")
//            viewModel.saveTransaction()
        }

        override fun abortTransaction() {
            Log.i("abort","call")
//            viewModel.abortTransaction()
            displayingDialog.show()
            displayingDialog.fullScreenProgressBar.indeterminateMode =true
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


                        Toast.makeText(context,
                            "Please only possibility to logout",
                            Toast.LENGTH_LONG)
                            .show()

                    }
                }

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        }

}
