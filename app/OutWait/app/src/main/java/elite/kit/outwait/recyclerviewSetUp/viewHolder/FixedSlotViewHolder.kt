package elite.kit.outwait.recyclerviewSetUp.viewHolder

import android.view.View
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import elite.kit.outwait.R
import elite.kit.outwait.waitingQueue.timeSlotModel.FixedTimeSlot

class FixedSlotViewHolder(private var itemView: View):BaseViewHolder<FixedTimeSlot>(itemView) {
    private var identifier = itemView.findViewById<TextView>(R.id.tvIdentifier)
    private var slotCode = itemView.findViewById<TextView>(R.id.tvSlotCode)
    private var icon = itemView.findViewById<TextView>(R.id.ivEditIcon)

    override fun bind(item: FixedTimeSlot) {
        TODO("Not yet implemented")
    }
}
