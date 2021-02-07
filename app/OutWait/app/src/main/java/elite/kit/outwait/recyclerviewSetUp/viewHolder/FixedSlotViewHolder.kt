package elite.kit.outwait.recyclerviewSetUp.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import elite.kit.outwait.R
import elite.kit.outwait.recyclerviewScreens.managmentViewScreen.ItemActionListener
import elite.kit.outwait.waitingQueue.timeSlotModel.FixedTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.SpontaneousTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot

class FixedSlotViewHolder(private var itemView: View, listener: ItemActionListener):BaseViewHolder<FixedTimeSlot>(itemView) {
    private var identifier = itemView.findViewById<TextView>(R.id.tvIdentifier)
    private var slotCode = itemView.findViewById<TextView>(R.id.tvSlotCode)
    private var icon = itemView.findViewById<ImageView>(R.id.ivEditIcon)
    private var container = itemView.findViewById<TextView>(R.id.tvFixedSlotContainer)

    init {

        container.setOnClickListener{
            if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                listener.onItemClicked(absoluteAdapterPosition)
            }
        }
        icon.setOnClickListener{
            if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                listener.editTimeSlot(absoluteAdapterPosition)
            }
        }
    }

    override fun bind(item: TimeSlot) {
        identifier.text = (item as FixedTimeSlot).auxiliaryIdentifier
        slotCode.text = item.slotCode
    }
}
