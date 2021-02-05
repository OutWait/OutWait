package elite.kit.outwait.recyclerviewSetUp.functionality

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import elite.kit.outwait.R
import elite.kit.outwait.recyclerviewScreens.managmentViewScreen.ItemActionListener
import elite.kit.outwait.recyclerviewScreens.managmentViewScreen.managmentViewFragment
import elite.kit.outwait.recyclerviewSetUp.viewHolder.BaseViewHolder
import elite.kit.outwait.recyclerviewSetUp.viewHolder.FixedSlotViewHolder
import elite.kit.outwait.recyclerviewSetUp.viewHolder.PauseSlotViewHolder
import elite.kit.outwait.recyclerviewSetUp.viewHolder.SpontaneousSlotViewHolder
import elite.kit.outwait.waitingQueue.timeSlotModel.*
import java.util.*

class SlotAdapter(slotList: MutableList<TimeSlot>, private val listener: ItemActionListener) : RecyclerView.Adapter<BaseViewHolder<*>>(),
    ItemTouchHelperAdapter {
    private lateinit var itemTouchHelper: ItemTouchHelper
    var slotList = slotList
       /* set {
            field =
            notifyDataSetChanged()
        }*/


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            Type.SPONTANEOUS_SLOT.ordinal ->
                SpontaneousSlotViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.spontaneous_slot, parent, false), itemTouchHelper
                )
            Type.FIXED_SLOT.ordinal ->
                FixedSlotViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.fixed_slot, parent, false)
                )
            else ->
                PauseSlotViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.pause_slot, parent, false)
                )
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = slotList!![position]
        when (element.getType()) {
            Type.SPONTANEOUS_SLOT -> holder.bind(element)
            Type.FIXED_SLOT -> holder.bind(element)
            Type.PAUSE -> holder.bind(element)
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


            for (i in fromPosition downTo toPosition + 1) {
                Log.i("move step", "$i")
                Collections.swap(slotList, i, i - 1)
            }

            Log.i("after move", "fromPos:$fromPosition toPos:$toPosition")

            this.notifyItemMoved(fromPosition, toPosition)
        }
    }

    override fun onItemSwiped(position: Int) {
        Log.i("swipe", "swipeeeeeeeeeeeeeeeeeeeeeeeeeee $position ")
        var removedSlot=slotList.get(position)
        slotList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(0, slotList.size - 2)
        listener.onItemSwiped(position,removedSlot)

    }

    override fun skipPauseSlots(position: Int) {
        var nextPos = position - 1

        Log.i("newPos", "$nextPos")
        if (nextPos >= 0 && getItemViewType(nextPos) == Type.PAUSE.ordinal) {

            while (getItemViewType(nextPos) == Type.PAUSE.ordinal) {
                //newPos!=0
                    Log.i("newPos", "$nextPos")
                nextPos--
                }

                onItemMove(position, nextPos)
            }
        }


    fun setTouchHelper(itemTouchHelper: ItemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper
    }


}
