package elite.kit.outwait.recyclerviewSetUp.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import elite.kit.outwait.R
import elite.kit.outwait.waitingQueue.timeSlotModel.SpontaneousTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot

class SpontaneousSlotViewHolder(private var itemView: View) :
    BaseViewHolder<SpontaneousTimeSlot>(itemView) {
    private var identifier = itemView.findViewById<TextView>(R.id.tvIdentifier)
    private var slotCode = itemView.findViewById<TextView>(R.id.tvSlotCode)
    private var icon = itemView.findViewById<ImageView>(R.id.ivEditIcon)
    override fun bind(item: TimeSlot) {
        identifier.text = (item as SpontaneousTimeSlot).auxiliaryIdentifier
        slotCode.text = (item as SpontaneousTimeSlot).slotCode
    }
}
