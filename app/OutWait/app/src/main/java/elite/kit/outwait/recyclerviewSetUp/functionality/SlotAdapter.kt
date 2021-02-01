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

class SlotAdapter(slotList: List<TimeSlot>) : RecyclerView.Adapter<BaseViewHolder<*>>(),
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
                        .inflate(R.layout.spontaneous_slot, parent, false),itemTouchHelper
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
        Log.i("skip", "fromPos:$fromPosition toPos:$toPosition newToPos: ")
        if (getItemViewType(fromPosition) == Type.SPONTANEOUS_SLOT.value) {

            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(slotList, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(slotList, i, i - 1)
                }
            }
            this.notifyItemMoved(fromPosition, toPosition)
        }
    }

    override fun onItemSwiped(position: Int) {
         var swipePos=position
    }

    override fun skipPauseSlots(position: Int) {
        var newPos=position
        do{
            newPos--
            Log.i("skip", "skippppppppppppppppeeeeeeeeeeee $newPos")

        }while (getItemViewType(newPos) == Type.PAUSE.value && newPos!=0 && getItemViewType(newPos-1) == Type.PAUSE.value )
        onItemMove(position, newPos)
    }

    fun setTouchHelpter(itemTouchHelper: ItemTouchHelper) {
        this.itemTouchHelper=itemTouchHelper
    }
}

