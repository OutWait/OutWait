package edu.kit.outwait.recyclerviewSetUp.viewHolder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import edu.kit.outwait.dataItem.TimeSlotItem
import edu.kit.outwait.waitingQueue.timeSlotModel.TimeSlot

/**
 * It is the basement of a viewholder in the recyclerview
 *
 * @param T Type of Item
 * @property itemView View of a item
 */
abstract class BaseViewHolder<T>(private  var itemView: View):RecyclerView.ViewHolder(itemView) {
    /**
     * Binds attributes of item with its layout
     *
     * @param item One of slot type
     */
    abstract  fun bind(item: TimeSlotItem)
}
