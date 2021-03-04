package elite.kit.outwait.recyclerviewSetUp.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import elite.kit.outwait.R
import elite.kit.outwait.dataItem.TimeSlotItem
import elite.kit.outwait.recyclerviewScreens.managementViewScreen.ItemActionListener
import elite.kit.outwait.utils.TransformationOutput
import elite.kit.outwait.waitingQueue.timeSlotModel.FixedTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.SpontaneousTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot

/**
 * Viewholder for the fixed slot
 *
 * @property itemViewFix View of a fixed slot
 * @constructor
 * Keeps listener on the view
 *
 * @param listener The ManagementViewFragment
 */
class FixedSlotViewHolder(private var itemViewFix: View, listener: ItemActionListener):BaseViewHolder<FixedTimeSlot>(itemViewFix) {
    private var identifier = itemViewFix.findViewById<TextView>(R.id.tvIdentifierFix)
    private var slotCode = itemViewFix.findViewById<TextView>(R.id.tvSlotCodeFix)
    private var startTime = itemViewFix.findViewById<TextView>(R.id.tvStartTimeFix)
    private var endTime = itemViewFix.findViewById<TextView>(R.id.tvEndTimeFix)
    private var icon = itemViewFix.findViewById<ImageView>(R.id.ivEditIconFix)
    private var container = itemViewFix.findViewById<ConstraintLayout>(R.id.tvSpoSlotContainerFix)

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

    /**
     * Binds attributes of item with its layout
     *
     * @param item Fixed slot
     */
    override fun bind(item: TimeSlotItem) {
        identifier.text = (item.timeSlot as FixedTimeSlot).auxiliaryIdentifier
        slotCode.text = (item.timeSlot as FixedTimeSlot).slotCode
        startTime.text = TransformationOutput.appointmentToString((item.timeSlot as FixedTimeSlot).interval.start)
        endTime.text = TransformationOutput.appointmentToString((item.timeSlot as FixedTimeSlot).interval.end)
    }
}
