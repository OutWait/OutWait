package edu.kit.outwait.recyclerviewSetUp.functionality


import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import edu.kit.outwait.R
import edu.kit.outwait.waitingQueue.timeSlotModel.Type

/**
 * It is a util class which adds functionalities to the recyclerview
 *
 * @property adapter Adapter from recyclerview
 */
class SlotItemTouchHelper(private var adapter: ItemTouchHelperAdapter) :
    ItemTouchHelper.Callback() {

    private var oldPos: Int = 0

    /**
     * Inform to starts drag and drop
     *
     * @return always true if the view is long pressed
     */
    override fun isLongPressDragEnabled(): Boolean {

        return true
    }

    /**
     * Inform whether the view is able to swipe
     *
     * @return always true if the view is swiping
     */
    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    /**
     * Informs the ManagementViewFragment whether a slot is moved
     *
     * @param recyclerView Recyclerview in which happened a action
     * @param viewHolder Performed action on this viewHolder
     */
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if (isViewHolderEnabledForDrag(viewHolder)) {
            if (viewHolder.absoluteAdapterPosition != -1) {
                adapter.registerMovement(viewHolder.absoluteAdapterPosition, oldPos)
            }
        }

        viewHolder.itemView.setBackgroundColor(
            ContextCompat.getColor(
                viewHolder.itemView.context,
                R.color.white
            )
        )
    }

    /**
     * Registers selection of viewHolder and allows it on specific viewHolder
     * Furthermore on selection background color of selected viewHolder chnages
     *
     * @param viewHolder
     * @param actionState
     */
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {


        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && isViewHolderEnabledForSelect(
                viewHolder!!
            )
        ) {
            viewHolder.itemView.setBackgroundColor(
                ContextCompat.getColor(
                    viewHolder.itemView.context,
                    R.color.outwait_color
                )
            )
            oldPos = viewHolder.absoluteAdapterPosition

        }

        super.onSelectedChanged(viewHolder, actionState)

    }

    /**
     * Returns flags which viewHolder can execute certain actions
     *
     * @param recyclerView Recyclerview in which happened a action
     * @param viewHolder Unspecific viewHolder
     * @return Integer which is constructed depended on set flags
     */
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
    ): Int {
        var dragFlags = 0
        if (isViewHolderEnabledForDrag(viewHolder)) {
            dragFlags = ItemTouchHelper.UP.or(ItemTouchHelper.DOWN)
        }
        var swipeFlag = 0
        if (isViewHolderEnabledForSwipe(viewHolder)) {
            swipeFlag = ItemTouchHelper.LEFT
        }
        return makeMovementFlags(dragFlags, swipeFlag)

    }

    /**
     * Executes a movement of a slot to another row
     *
     * @param recyclerView Recyclerview in which happened the movement
     * @param viewHolder Moved viewHolder
     * @param target Target viewHolder
     * @return
     */
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        adapter.onItemMove(viewHolder.absoluteAdapterPosition, target.absoluteAdapterPosition)
        return true
    }

    /**
     * Executes a swipe on a viewHolder
     *
     * @param viewHolder Swiped viewHolder
     * @param direction Set direction before in getMovementFlags()
     */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.absoluteAdapterPosition
        adapter.onItemSwiped(position)
    }

    /**
     * Determines which viewHolder can be dragged and dropped
     *
     * @param viewHolder Dragged and Dropped viewHolder
     * @return viewHolder able to drag and drop-> true, else false
     */
    private fun isViewHolderEnabledForDrag(viewHolder: RecyclerView.ViewHolder): Boolean {
        return when (viewHolder.itemViewType) {
            Type.SPONTANEOUS_SLOT.ordinal -> true
            else -> false
        }
    }

    /**
     * Determines which viewHolder can be swiped
     *
     * @param viewHolder Swiped ViewHolder
     * @return viewHolder can be swiped-> true, else false
     */
    private fun isViewHolderEnabledForSwipe(viewHolder: RecyclerView.ViewHolder): Boolean {
        return when (viewHolder.itemViewType) {
            Type.PAUSE.ordinal -> false
            Type.HEADER.ordinal -> false
            else -> true
        }
    }

    /**
     * Determines which viewHolder can be selected
     *
     * @param viewHolder Selected viewHolder
     * @return viewHolder can be selected-> true, else false
     */
    private fun isViewHolderEnabledForSelect(viewHolder: RecyclerView.ViewHolder): Boolean {
        return when (viewHolder.itemViewType) {
            Type.PAUSE.ordinal -> false
            Type.HEADER.ordinal -> false
            else -> true
        }
    }
}
