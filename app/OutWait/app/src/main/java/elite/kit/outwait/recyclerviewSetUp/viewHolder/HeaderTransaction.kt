package elite.kit.outwait.recyclerviewSetUp.viewHolder

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import elite.kit.outwait.R
import elite.kit.outwait.recyclerviewScreens.managementViewScreen.ItemActionListener
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot

class HeaderTransaction(
    private var itemView: View, private var listener: ItemActionListener,
    ) : BaseViewHolder<HeaderTransaction>(itemView){
    private var iconSave = itemView.findViewById<ImageView>(R.id.ivSaveTransaction)
    private var iconAbort = itemView.findViewById<ImageView>(R.id.ivAbortTransaction)

    init {
        iconSave.setOnClickListener{
            if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                listener.saveTransaction()
            }
        }
        iconAbort.setOnClickListener{
            if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                listener.abortTransaction()
            }
        }
    }
    override fun bind(item: TimeSlot) {
        item.getType()
    }
}
