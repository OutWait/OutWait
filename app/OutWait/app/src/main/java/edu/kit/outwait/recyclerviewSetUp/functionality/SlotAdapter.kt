package edu.kit.outwait.recyclerviewSetUp.functionality

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import edu.kit.outwait.R
import edu.kit.outwait.dataItem.DataItem
import edu.kit.outwait.dataItem.TimeSlotItem
import edu.kit.outwait.recyclerviewScreens.managementViewScreen.ItemActionListener
import edu.kit.outwait.recyclerviewSetUp.viewHolder.BaseViewHolder
import edu.kit.outwait.recyclerviewSetUp.viewHolder.FixedSlotViewHolder
import edu.kit.outwait.recyclerviewSetUp.viewHolder.PauseSlotViewHolder
import edu.kit.outwait.recyclerviewSetUp.viewHolder.SpontaneousSlotViewHolder
import edu.kit.outwait.recyclerviewScreens.managementViewScreen.ManagementViewFragment
import edu.kit.outwait.recyclerviewSetUp.viewHolder.*
import edu.kit.outwait.utils.EspressoIdlingResource
import edu.kit.outwait.waitingQueue.timeSlotModel.*
import kotlinx.android.synthetic.main.full_screen_progress_bar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.joda.time.Interval
import java.util.*

/**
 * This is a adapter which transforms a slot to  a visible viewHolder with slot information in the recyclerview
 *
 * @property listener ManagementViewFragment
 * @constructor
 *
 *
 * @param slotList List of current slots
 */
private const val START_TIME = 0L
private const val END_TIME = 0L
private const val HEADER = "header"
private const val FIRST_POSITION_TRANSACTION=1
private const val FIRST_POSITION_LIST=0
private const val ONE=1
private const val TWO=2


class SlotAdapter(slotList: MutableList<DataItem>, private val listener: ItemActionListener) :
    RecyclerView.Adapter<BaseViewHolder<*>>(),
    ItemTouchHelperAdapter {
    private lateinit var itemTouchHelper: ItemTouchHelper


    /**
     * Current slot list from the server
     */
    var slotList = slotList

    /**
     * Updates the slot list with the passed list
     *
     * @param newTimeSlotList List of latest slots
     */
    fun updateSlots(newTimeSlotList: MutableList<DataItem>?) {

            slotList.clear()
            notifyDataSetChanged()
            if (newTimeSlotList != null) {
                slotList.addAll(newTimeSlotList)
            }
            notifyDataSetChanged()

    }

    /**
     * Assigns a slot to its viewHolder
     *
     * @param parent Recyclerview
     * @param viewType Type of slot
     * @return ViewHolder of slot
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            Type.SPONTANEOUS_SLOT.ordinal ->
                SpontaneousSlotViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.spontaneous_slot, parent, false),
                    itemTouchHelper,
                    listener
                )
            Type.FIXED_SLOT.ordinal ->
                FixedSlotViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.fixed_slot, parent, false),
                    listener
                )
            Type.PAUSE.ordinal ->
                PauseSlotViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.pause_slot, parent, false)
                )

            else -> HeaderTransaction(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.header_transaction, parent, false), listener
            )
        }
    }

    /**
     * Assigns by slot position to its viewHolder to bind data from slot to viewHolder
     *
     * @param holder ViewHolder of slot
     * @param position Current position of slot
     */
    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = slotList!![position]
        when (slotList!![position].getType().ordinal) {
            Type.SPONTANEOUS_SLOT.ordinal -> holder.bind(element as TimeSlotItem)
            Type.FIXED_SLOT.ordinal -> holder.bind(element as TimeSlotItem)
            Type.PAUSE.ordinal -> holder.bind(element as TimeSlotItem)
            Type.HEADER.ordinal -> holder.bind(
                TimeSlotItem(
                    SpontaneousTimeSlot(
                        Interval(
                            START_TIME,
                            END_TIME
                        ), HEADER, HEADER
                    )
                )
            )
            else -> throw IllegalArgumentException()
        }
    }

    /**
     * Returns size of current list
     *
     * @return Size of list
     */
    override fun getItemCount(): Int {
        return slotList!!.size
    }

    /**
     * Returns Type of slot
     *
     * @param position Current slot
     * @return Type of slot
     */
    override fun getItemViewType(position: Int): Int {
        val item = slotList!![position]
        return item.getType().ordinal
    }

    /**
     * Executes a movement of a slot
     *
     * @param fromPosition Start position
     * @param toPosition Final position
     */
    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (getItemViewType(fromPosition) == Type.SPONTANEOUS_SLOT.ordinal) {
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(slotList, i, i + ONE)
                }
            } else {
                for (i in fromPosition downTo toPosition + ONE) {
                    Collections.swap(slotList, i, i - ONE)
                }
            }
            this.notifyItemMoved(fromPosition, toPosition)
        }
    }

    /**
     * Performs a swipe on a slot
     *
     * @param position Swiped slot position
     */
    override fun onItemSwiped(position: Int) {
        var removedSlot = (slotList[position] as TimeSlotItem)
        slotList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(FIRST_POSITION_LIST, slotList.size - TWO)
        listener.onItemSwiped(position, removedSlot)
    }

    /**
     * Registers a movement which is reported to ManagementViewFragment
     *
     * @param newPos Final position
     * @param oldPos start position
     */
    override fun registerMovement(newPos: Int, oldPos: Int) {

        ManagementViewFragment.displayingDialog.show()
        ManagementViewFragment.displayingDialog.fullScreenProgressBar.indeterminateMode = true

        if (slotList[FIRST_POSITION_LIST].getType() == Type.HEADER && newPos == FIRST_POSITION_TRANSACTION || newPos == FIRST_POSITION_LIST) {
            onItemMove(newPos, oldPos)
            ManagementViewFragment.displayingDialog.dismiss()
        } else {

            var movedSlot =
                ((slotList[newPos] as TimeSlotItem).timeSlot as ClientTimeSlot).slotCode
            var otherSlot =
                ((slotList[newPos - ONE] as TimeSlotItem).timeSlot as ClientTimeSlot).slotCode
            var list = mutableListOf<String>()
            list.add(movedSlot)
            list.add(otherSlot)
            ManagementViewFragment.movementInfo.value = list
        }

    }

    /**
     * Set itemTochHleper from adapter
     *
     * @param itemTouchHelper Instance with functionalites for the recyclerview
     */
    fun setTouchHelper(itemTouchHelper: ItemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper
    }


}
