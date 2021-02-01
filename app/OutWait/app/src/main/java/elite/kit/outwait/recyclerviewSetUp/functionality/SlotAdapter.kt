package elite.kit.outwait.recyclerviewSetUp.functionality

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import elite.kit.outwait.R
import elite.kit.outwait.recyclerviewSetUp.viewHolder.BaseViewHolder
import elite.kit.outwait.recyclerviewSetUp.viewHolder.FixedSlotViewHolder
import elite.kit.outwait.recyclerviewSetUp.viewHolder.PauseSlotViewHolder
import elite.kit.outwait.recyclerviewSetUp.viewHolder.SpontaneousSlotViewHolder
import elite.kit.outwait.waitingQueue.timeSlotModel.*
import java.util.*

class SlotAdapter(slotList: MutableList<TimeSlot>) : RecyclerView.Adapter<BaseViewHolder<*>>(),
    ItemTouchHelperAdapter {
    private lateinit var itemTouchHelper: ItemTouchHelper
    var slotList = slotList
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            Type.SPONTANEOUS_SLOT.value ->
                SpontaneousSlotViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.spontaneous_slot, parent, false), itemTouchHelper
                )
            Type.FIXED_SLOT.value ->
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
            Type.SPONTANEOUS_SLOT.value -> holder.bind(element)
            Type.FIXED_SLOT.value -> holder.bind(element)
            Type.PAUSE.value -> holder.bind(element)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemCount(): Int {
        return slotList!!.size
    }

    override fun getItemViewType(position: Int): Int {
        val item = slotList!![position]
        return item.getType()
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Log.i("before move", "fromPos:$fromPosition toPos:$toPosition")
        if (getItemViewType(fromPosition) == Type.SPONTANEOUS_SLOT.value) {


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
        slotList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(0, slotList.size - 1)
    }

    override fun skipPauseSlots(position: Int) {
        var nextPos = position - 1

        Log.i("newPos", "$nextPos")
        if (nextPos >= 0 && getItemViewType(nextPos) == Type.PAUSE.value) {

            while (getItemViewType(nextPos) == Type.PAUSE.value) {
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
