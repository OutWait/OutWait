package elite.kit.outwait.recyclerviewSetUp.viewHolder

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import elite.kit.outwait.R
import elite.kit.outwait.recyclerviewScreens.managementViewScreen.ItemActionListener
import elite.kit.outwait.waitingQueue.timeSlotModel.SpontaneousTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot

class SpontaneousSlotViewHolder(
    private var itemView: View,
    private var itemTouchHelper: ItemTouchHelper,
    listener: ItemActionListener
) :
    BaseViewHolder<SpontaneousTimeSlot>(itemView),View.OnTouchListener, GestureDetector.OnGestureListener {
    private var identifier = itemView.findViewById<TextView>(R.id.tvIdentifier)
    private var slotCode = itemView.findViewById<TextView>(R.id.tvSlotCode)
    private var icon = itemView.findViewById<ImageView>(R.id.ivEditIcon)
    private var container = itemView.findViewById<TextView>(R.id.tvSpoSlotContainer)

    private val gestureDetector: GestureDetector = GestureDetector(itemView.context, this)

    init {
        itemView.setOnTouchListener(this)

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
        identifier.text = (item as SpontaneousTimeSlot).auxiliaryIdentifier
        slotCode.text = (item as SpontaneousTimeSlot).slotCode
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float,
    ): Boolean {
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
        itemTouchHelper.startDrag(this)
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float,
    ): Boolean {
        return true
    }
}
