package elite.kit.outwait.recyclerviewSetUp.viewHolder

import android.view.View
import android.widget.TextView
import elite.kit.outwait.R
import elite.kit.outwait.waitingQueue.timeSlotModel.Pause

class PauseSlotViewHolder(private var itemView: View):BaseViewHolder<Pause>(itemView) {
    private var pauseTextView = itemView.findViewById<TextView>(R.id.tvPause)
    override fun bind(item: Pause) {
        pauseTextView.text="Free time"
    }

}
