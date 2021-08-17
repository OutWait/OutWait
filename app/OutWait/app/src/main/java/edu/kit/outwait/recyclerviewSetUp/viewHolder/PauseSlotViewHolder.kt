package edu.kit.outwait.recyclerviewSetUp.viewHolder

import android.view.View
import android.widget.TextView
import edu.kit.outwait.R
import edu.kit.outwait.dataItem.TimeSlotItem
import edu.kit.outwait.waitingQueue.timeSlotModel.Pause
import edu.kit.outwait.waitingQueue.timeSlotModel.TimeSlot

/**
 * Viewholder for a pause slot
 *
 * @property itemView View of a pause slot
 */
class PauseSlotViewHolder(private var itemView: View):BaseViewHolder<Pause>(itemView) {
    /**
     * Binds attributes of item with its layout
     *
     * @param item Slot
     */
    override fun bind(item: TimeSlotItem) {
    }

}
