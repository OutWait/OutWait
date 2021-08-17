package edu.kit.outwait.recyclerviewSetUp.viewHolder

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import edu.kit.outwait.R
import edu.kit.outwait.dataItem.TimeSlotItem
import edu.kit.outwait.recyclerviewScreens.managementViewScreen.ItemActionListener
import edu.kit.outwait.waitingQueue.timeSlotModel.TimeSlot

/**
 * Viewholder for the header item
 *
 * @property itemViewHeader View of the header
 * @property listener ManagementViewFragment
 */
class HeaderTransaction(
    private var itemViewHeader: View, private var listener: ItemActionListener,
) : BaseViewHolder<HeaderTransaction>(itemViewHeader) {
    private var iconSave = itemViewHeader.findViewById<ImageView>(R.id.ivSaveTransaction)
    private var iconAbort = itemViewHeader.findViewById<ImageView>(R.id.ivAbortTransaction)

    init {
        iconSave.setOnClickListener {
            if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                listener.saveTransaction()
            }
        }
        iconAbort.setOnClickListener {
            if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                listener.abortTransaction()
            }
        }
    }

    /**
     * Binds attributes of item with its layout
     *
     * @param item Slot
     */
    override fun bind(item: TimeSlotItem) {
    }
}
