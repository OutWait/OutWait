package edu.kit.outwait.recyclerviewScreens.managementViewScreen

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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.Callback
import dagger.hilt.android.AndroidEntryPoint
import edu.kit.outwait.R
import edu.kit.outwait.dataItem.DataItem
import edu.kit.outwait.dataItem.TimeSlotItem
import edu.kit.outwait.dataItem.HeaderItem
import edu.kit.outwait.databinding.ManagementViewFragmentBinding
import edu.kit.outwait.recyclerviewScreens.editSlotDialog.EditTimeSlotDialogFragment
import edu.kit.outwait.recyclerviewScreens.slotDetailDialog.SlotDetailDialogFragment
import edu.kit.outwait.recyclerviewSetUp.functionality.SlotAdapter
import edu.kit.outwait.recyclerviewSetUp.functionality.SlotItemTouchHelper
import edu.kit.outwait.recyclerviewSetUp.viewHolder.HeaderTransaction
import edu.kit.outwait.utils.EspressoIdlingResource
import edu.kit.outwait.waitingQueue.timeSlotModel.*
import kotlinx.android.synthetic.main.full_screen_progress_bar.*
import kotlinx.android.synthetic.main.management_view_fragment.*

/**
 * This is a dialog to keeps the slot queue
 *
 */
private const val FIRST_POSITION = 0
private const val CURRENT_SLOT1 = 0
private const val CURRENT_SLOT2 = 1
private const val TWO = 2

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

    /**
     * Creates layout and uses data binding
     *
     * @param inflater Inflates layout with data binding
     * @param container Keeper of layout
     * @param savedInstanceState Passed data from previous fragment
     * @return layout with data binding
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.management_view_fragment, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
        binding.slotList.layoutManager = LinearLayoutManager(activity)
        binding.slotList.setHasFixedSize(true)
        builder = AlertDialog.Builder(activity)
        builder.apply {
            setView(R.layout.full_screen_progress_bar)
            setTitle(getString(R.string.process_title))
            //TODO SET FALSE BEFORE PASS IT TO REPORTERS
            setCancelable(false)
        }
        displayingDialog = builder.create()


        viewModel.slotQueue.observe(viewLifecycleOwner, Observer { list ->
            var itemList: MutableList<DataItem> = list.toMutableList().map {
                TimeSlotItem(it)
            }.toMutableList()

            if (viewModel.isInTransaction.value!!) {
                itemList.add(FIRST_POSITION, HeaderItem())
                slotAdapter.updateSlots(itemList)

            } else {
                slotAdapter.updateSlots(itemList)
            }
            displayingDialog.dismiss()
        })

        viewModel.isLoggedIn.observe(viewLifecycleOwner, Observer { isLoggedIn ->
            if (!isLoggedIn && viewModel.isInTransaction.value!!) {
                deleteHeader()
            }

            if (!isLoggedIn) {
                findNavController().popBackStack()
            }
        })

        viewModel.isInTransaction.observe(viewLifecycleOwner, Observer { isInTransaction->
            if(!isInTransaction){
                displayingDialog.dismiss()
            }
        })


        movementInfo.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                viewModel.moveSlotAfterAnother(it.first(), it.last())
                displayingDialog.dismiss()
            }
        })

        slotAdapter = SlotAdapter(mutableListOf<DataItem>(), this)
        var callback: ItemTouchHelper.Callback = SlotItemTouchHelper(slotAdapter)
        var itemTouchHelper: ItemTouchHelper = ItemTouchHelper(callback)
        slotAdapter.setTouchHelper(itemTouchHelper)
        itemTouchHelper.attachToRecyclerView(binding.slotList)
        binding.slotList.adapter = slotAdapter

        setHasOptionsMenu(true)

        exitApp()
        return binding.root
    }

    /**
     * Displays a dialog to show all details of a slot
     *
     * @param position Selected slot
     */
    override fun onItemClicked(position: Int) {
        var detailDialog =
            SlotDetailDialogFragment((slotAdapter.slotList[position] as TimeSlotItem).timeSlot as ClientTimeSlot)
        detailDialog.show(childFragmentManager, "ssss")
    }

    /**
     * Executes remove of a slot
     *
     * @param position Position of a removed slot
     * @param removedSlot Removed slot
     */
    override fun onItemSwiped(position: Int, removedSlot: TimeSlotItem) {
        EspressoIdlingResource.increment()
        notifyDeleteSlot(position, removedSlot.timeSlot)
        EspressoIdlingResource.decrement()

    }

    /**
     * Notifies repository that a slot is deleted
     *
     * @param position Position of removed slot
     * @param removedSlot Removed slot
     */
    private fun notifyDeleteSlot(position: Int, removedSlot: TimeSlot) {
        var removedClientSlot = removedSlot as ClientTimeSlot
        var firstPosition = if (viewModel.isInTransaction.value!!) CURRENT_SLOT2 else CURRENT_SLOT1
        when (position) {
            firstPosition -> viewModel.deleteCurrentSlot()
            else -> {
                viewModel.deleteSlot(removedClientSlot.slotCode)
                slotAdapter.updateSlots(slotAdapter.slotList.toMutableList())
                displayingDialog.show()
                displayingDialog.fullScreenProgressBar.indeterminateMode = true
            }
        }

    }

    /**
     * Displays a dialog to edit slot
     *
     * @param position Selected slot
     */
    override fun editTimeSlot(position: Int) {
        var editDialog =
            EditTimeSlotDialogFragment((slotAdapter.slotList[position] as TimeSlotItem).timeSlot as ClientTimeSlot)
        editDialog.show(childFragmentManager, "aaa")
    }

    /**
     * Notifies that the transaction is saved
     *
     */
    override fun saveTransaction() {
        viewModel.saveTransaction()
        deleteHeader()
    }

    /**
     * Notifies that the transaction is aborted
     *
     */
    override fun abortTransaction() {
        viewModel.abortTransaction()
        deleteHeader()
        displayingDialog.show()
        displayingDialog.fullScreenProgressBar.indeterminateMode = true
    }

    /**
     * Deletes header (transaction panel) of queue
     *
     */
    private fun deleteHeader() {
        slotAdapter.slotList.removeAt(FIRST_POSITION)
        slotAdapter.notifyItemRemoved(FIRST_POSITION)
        slotAdapter.notifyItemRangeChanged(FIRST_POSITION, slotAdapter.slotList.size - TWO)
    }

    /**
     * Returns an identifier
     *
     * @param slot Selected slot
     * @return identifier of selected slot
     */
    private fun getIdentifier(slot: TimeSlot): String {
        return (slot as ClientTimeSlot).auxiliaryIdentifier
    }

    /**
     * Displays a custom menu
     *
     * @param menu Menu with items
     * @param inflater Inflates menu
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.overflow, menu)

    }

    /**
     * Navigates to configDialog by clicking icon setting
     *
     * @param item Selected item
     * @return always true
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.navigateToConfigDialog()
        return true
    }

    /**
     * Avoids back press, only possible to logout in the settings
     *
     */
    private fun exitApp() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Toast.makeText(
                        context,
                        getString(R.string.text_avoid_back_press_management),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

}
