package elite.kit.outwait.recyclerviewSetUp.functionality

import android.graphics.Color
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import elite.kit.outwait.R
import elite.kit.outwait.waitingQueue.timeSlotModel.Type

class SlotItemTouchHelper(private var adapter: ItemTouchHelperAdapter) :
    ItemTouchHelper.Callback() {

    private var oldPos: Int = 0

    override fun isLongPressDragEnabled(): Boolean {

        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        //TODO ehance algo to skip pause slots
        /*if (isViewHolderEnabledForDrag(viewHolder)) {
            if (viewHolder.absoluteAdapterPosition != -1) {
                //Maybe oldPos is useless
                if (viewHolder.absoluteAdapterPosition < oldPos) {
                    Log.i("first case", "${viewHolder.absoluteAdapterPosition}")
                    adapter.skipPauseSlots(viewHolder.absoluteAdapterPosition)
                }

            }
        }*/


//        viewHolder.itemView.setBackgroundColor(
//            ContextCompat.getColor(
//                viewHolder.itemView.context,
//                 Color.parseColor("#F44336").toInt() )      )
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {


        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && isViewHolderEnabledForSelect(
                viewHolder!!)
        ) {
            // viewHolder!!.itemView.setBackgroundColor(Color.YELLOW)
            oldPos = viewHolder.absoluteAdapterPosition


        }

        super.onSelectedChanged(viewHolder, actionState)

    }


    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
    ): Int {
        var dragFlags = 0
        if (isViewHolderEnabledForDrag(viewHolder)) {
            dragFlags = ItemTouchHelper.UP.or(ItemTouchHelper.DOWN)
        }
        var swiipeFlag = 0
        if (isViewHolderEnabledForSwipe(viewHolder)) {
            swiipeFlag = ItemTouchHelper.LEFT
        }
        return makeMovementFlags(dragFlags, swiipeFlag)

    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        adapter.onItemMove(viewHolder.absoluteAdapterPosition, target.absoluteAdapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.absoluteAdapterPosition
        adapter.onItemSwiped(position)
    }


    private fun isViewHolderEnabledForDrag(viewHolder: RecyclerView.ViewHolder): Boolean {
        return when (viewHolder.itemViewType) {
            Type.SPONTANEOUS_SLOT.ordinal -> true
            else -> false
        }
    }

    private fun isViewHolderEnabledForSwipe(viewHolder: RecyclerView.ViewHolder): Boolean {
        return when (viewHolder.itemViewType) {
            Type.PAUSE.ordinal -> false
            else -> true
        }
    }

    private fun isViewHolderEnabledForSelect(viewHolder: RecyclerView.ViewHolder): Boolean {
        return when (viewHolder.itemViewType) {
            Type.PAUSE.ordinal -> false
            else -> true
        }
    }
}
