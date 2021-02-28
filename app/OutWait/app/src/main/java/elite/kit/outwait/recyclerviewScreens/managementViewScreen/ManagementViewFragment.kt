package elite.kit.outwait.recyclerviewScreens.managementViewScreen

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.size
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.Callback
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.R
import elite.kit.outwait.dataItem.DataItem
import elite.kit.outwait.dataItem.TimeSlotItem
import elite.kit.outwait.dataItem.HeaderItem
import elite.kit.outwait.databinding.ManagementViewFragmentBinding
import elite.kit.outwait.recyclerviewScreens.editSlotDialog.EditTimeSlotDialogFragment
import elite.kit.outwait.recyclerviewScreens.slotDetailDialog.SlotDetailDialogFragment
import elite.kit.outwait.recyclerviewSetUp.functionality.SlotAdapter
import elite.kit.outwait.recyclerviewSetUp.functionality.SlotItemTouchHelper
import elite.kit.outwait.recyclerviewSetUp.viewHolder.HeaderTransaction
import elite.kit.outwait.waitingQueue.timeSlotModel.*
import kotlinx.android.synthetic.main.full_screen_progress_bar.*

@AndroidEntryPoint
class ManagementViewFragment : Fragment(), ItemActionListener {


    private val viewModel: ManagementViewViewModel by viewModels()
    private lateinit var binding: ManagementViewFragmentBinding

    companion object {
        lateinit var displayingDialog: AlertDialog
        var movementInfo = MutableLiveData(mutableListOf<String>())
    }

    private lateinit var slotAdapter: SlotAdapter
    private lateinit var builder: AlertDialog.Builder
    private var FIRST_POSITION = 0
    private var CURREND_SLOT1 = 0
    private var CURREND_SLOT2 = 1


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
        displayingDialog = builder.create()


        viewModel.slotQueue.observe(viewLifecycleOwner) { list ->
            var itemList: MutableList<DataItem> = list.toMutableList().map {
                TimeSlotItem(it)
            }.toMutableList()
            Log.i("observeQUEUE", "VALUE:  ${viewModel.isInTransaction.value}")

            if (viewModel.isInTransaction.value!!) {
                itemList.add(FIRST_POSITION, HeaderItem())
                slotAdapter.updateSlots(itemList)

            } else {
                slotAdapter.updateSlots(itemList)
            }
            displayingDialog.dismiss()
        }


        movementInfo.observe(viewLifecycleOwner) {

          if(it.isNotEmpty()) {
              viewModel.moveSlotAfterAnother(it.first(), it.last())
                displayingDialog.dismiss()
          }
        }

        viewModel.isInTransaction.observe(viewLifecycleOwner) {
            Log.i("observerTransaction", "VALUE:  ${viewModel.isInTransaction.value}")

            //TODO what to do if transaction is denied ?
            /* if (it) {
                 Log.i("inTransaction", "${viewModel.isInTransaction.value}")
                 var newList= slotAdapter.slotList.toMutableList()
                 newList.add(FIRST_POSITION, HeaderItem())
                 slotAdapter.updateSlots(newList)
                 } else if (!it && !firstTime) {
                 var newList= slotAdapter.slotList.toMutableList()
                 newList.removeAt(FIRST_POSITION)
                 slotAdapter.updateSlots(newList)
             }*/
        }


        //Add listener for recyclerview

        slotAdapter = SlotAdapter(mutableListOf<DataItem>(), this)
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


    fun forwarderMove(movedSlot: String, otherSlot: String) {
        viewModel.moveSlotAfterAnother(movedSlot, otherSlot)
    }

    override fun onItemClicked(position: Int) {
        var detailDialog =
            SlotDetailDialogFragment((slotAdapter.slotList[position] as TimeSlotItem).timeSlot as ClientTimeSlot)
        detailDialog.show(childFragmentManager, "ssss")
    }


    override fun onItemSwiped(position: Int, removedSlot: TimeSlotItem) {
        //TODO block delete of multiple slots simuntanously
        var resetDelete =
            Snackbar.make(binding.slotList,
                "${getIdentifier(removedSlot.timeSlot)}",
                Snackbar.LENGTH_LONG)
                .setAction(getString(
                    R.string.undo)) {
                    slotAdapter.slotList.add(position, removedSlot)
                    slotAdapter.notifyItemInserted(position)
                    slotAdapter.notifyItemRangeChanged(0, slotAdapter.slotList.size - 1)
                }

        resetDelete.addCallback(object : Callback() {
            override fun onDismissed(snackbar: Snackbar, event: Int) {
                super.onDismissed(snackbar, event)
                if (event == DISMISS_EVENT_TIMEOUT) {
                    notifyDeleteSlot(position, removedSlot.timeSlot)

                }
            }
        })
        resetDelete.show()
    }

    private fun notifyDeleteSlot(position: Int, removedSlot: TimeSlot) {
        var removedClientSlot = removedSlot as ClientTimeSlot
        var firstPosition = if (viewModel.isInTransaction.value!!) CURREND_SLOT2 else CURREND_SLOT1
        when (position) {
            firstPosition -> viewModel.deleteCurrentSlot()
            else -> {
                viewModel.deleteSlot(removedClientSlot.slotCode)

                //TODO  except response from server to dismiss
                slotAdapter.slotList.add(FIRST_POSITION, HeaderItem())
                slotAdapter.updateSlots(slotAdapter.slotList.toMutableList())
                displayingDialog.show()
                displayingDialog.fullScreenProgressBar.indeterminateMode = true
            }
        }

    }

    override fun editTimeSlot(position: Int) {
        var editDialog =
            EditTimeSlotDialogFragment((slotAdapter.slotList[position] as TimeSlotItem).timeSlot as ClientTimeSlot)
        editDialog.show(childFragmentManager, "aaa")
    }


    override fun saveTransaction() {
        Log.i("save", "call")
        viewModel.saveTransaction()
        deleteHeader()
    }

    override fun abortTransaction() {
        Log.i("abort", "call")
        viewModel.abortTransaction()
        deleteHeader()
        displayingDialog.show()
        displayingDialog.fullScreenProgressBar.indeterminateMode = true
    }

    private fun deleteHeader() {
        slotAdapter.slotList.removeAt(FIRST_POSITION)
        slotAdapter.notifyItemRemoved(FIRST_POSITION)
        slotAdapter.notifyItemRangeChanged(0, slotAdapter.slotList.size - 2)
    }


    private fun getIdentifier(slot: TimeSlot): String {
        //Guarantee slot is only fixed or spo by GUI
        return (slot as ClientTimeSlot).auxiliaryIdentifier
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
