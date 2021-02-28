package elite.kit.outwait.recyclerviewSetUp.functionality

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import elite.kit.outwait.R
import elite.kit.outwait.dataItem.DataItem
import elite.kit.outwait.dataItem.TimeSlotItem
import elite.kit.outwait.recyclerviewScreens.managementViewScreen.ItemActionListener
import elite.kit.outwait.recyclerviewSetUp.viewHolder.BaseViewHolder
import elite.kit.outwait.recyclerviewSetUp.viewHolder.FixedSlotViewHolder
import elite.kit.outwait.recyclerviewSetUp.viewHolder.PauseSlotViewHolder
import elite.kit.outwait.recyclerviewSetUp.viewHolder.SpontaneousSlotViewHolder
import elite.kit.outwait.recyclerviewScreens.managementViewScreen.ManagementViewFragment
import elite.kit.outwait.recyclerviewSetUp.viewHolder.*
import elite.kit.outwait.waitingQueue.timeSlotModel.*
import kotlinx.android.synthetic.main.full_screen_progress_bar.*
import org.joda.time.Interval
import java.util.*

class SlotAdapter(slotList: MutableList<DataItem>, private val listener: ItemActionListener) :
    RecyclerView.Adapter<BaseViewHolder<*>>(),
    ItemTouchHelperAdapter {
    private lateinit var itemTouchHelper: ItemTouchHelper
    var slotList = slotList


    fun updateSlots(newTimeSlotList: MutableList<DataItem>?) {
        slotList.clear()
        notifyDataSetChanged()
        if (newTimeSlotList != null) {
            slotList.addAll(newTimeSlotList)
        }
        notifyDataSetChanged()
    }


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

            else -> HeaderTransaction(LayoutInflater.from(parent.context)
                .inflate(R.layout.header_transaction, parent, false), listener)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = slotList!![position]
        when (slotList!![position].getType().ordinal) {

            Type.SPONTANEOUS_SLOT.ordinal -> holder.bind(element as TimeSlotItem)
            Type.FIXED_SLOT.ordinal -> holder.bind(element as TimeSlotItem)
            Type.PAUSE.ordinal -> holder.bind(element as TimeSlotItem)
            Type.HEADER.ordinal -> holder.bind(TimeSlotItem(SpontaneousTimeSlot(Interval(20L, 22L), "ss", "aa")))
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemCount(): Int {
        return slotList!!.size
    }

    override fun getItemViewType(position: Int): Int {
        val item = slotList!![position]
        return item.getType().ordinal
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Log.i("before move", "fromPos:$fromPosition toPos:$toPosition")
        if (getItemViewType(fromPosition) == Type.SPONTANEOUS_SLOT.ordinal) {


            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(slotList, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(slotList, i, i - 1)
                }
            }

            Log.i("after move", "fromPos:$fromPosition toPos:$toPosition")

            this.notifyItemMoved(fromPosition, toPosition)
        }
    }

    override fun onItemSwiped(position: Int) {
        Log.i("swipe", "swipeeeeeeeeeeeeeeeeeeeeeeeeeee $position ")
        var removedSlot = (slotList[position] as TimeSlotItem)
        slotList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(0, slotList.size - 2)
        listener.onItemSwiped(position, removedSlot)
    }

    override fun skipPauseSlots(position: Int) {

        //TODO block movement before first


        var movedSlot = ((slotList[position] as TimeSlotItem).timeSlot as ClientTimeSlot).slotCode
        var otherSlot = ((slotList[position-1] as TimeSlotItem).timeSlot as ClientTimeSlot).slotCode
        var list = mutableListOf<String>()
        list.add(movedSlot)
        list.add(otherSlot)
        ManagementViewFragment.displayingDialog.show()
        ManagementViewFragment.displayingDialog.fullScreenProgressBar.indeterminateMode=true

        ManagementViewFragment.movementInfo.value = list
        Log.i("input", "${ManagementViewFragment.movementInfo.value}")

    }


    fun setTouchHelper(itemTouchHelper: ItemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper
    }


}
