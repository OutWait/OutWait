package elite.kit.outwait.recyclerviewSetUp.viewHolder

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import elite.kit.outwait.R
import elite.kit.outwait.dataItem.TimeSlotItem
import elite.kit.outwait.recyclerviewScreens.managementViewScreen.ItemActionListener
import elite.kit.outwait.utils.TransformationOutput
import elite.kit.outwait.waitingQueue.timeSlotModel.SpontaneousTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot

class SpontaneousSlotViewHolder(
    private var itemViewSpo: View,
    private var itemTouchHelper: ItemTouchHelper,
    listener: ItemActionListener
) :
    BaseViewHolder<SpontaneousTimeSlot>(itemViewSpo),View.OnTouchListener, GestureDetector.OnGestureListener {
    private var identifier = itemViewSpo.findViewById<TextView>(R.id.tvIdentifierSpo)
    private var slotCode = itemViewSpo.findViewById<TextView>(R.id.tvSlotCodeSpo)
    private var startTime = itemViewSpo.findViewById<TextView>(R.id.tvStartTimeSpo)
    private var endTime = itemViewSpo.findViewById<TextView>(R.id.tvEndTimeSpo)

    private var icon = itemViewSpo.findViewById<ImageView>(R.id.ivEditIconSpo)
    private var container = itemViewSpo.findViewById<ConstraintLayout>(R.id.tvSpoSlotContainerSpo)

    private val gestureDetector: GestureDetector = GestureDetector(itemViewSpo.context, this)

    init {
        itemViewSpo.setOnTouchListener(this)

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

    override fun bind(item: TimeSlotItem) {
        identifier.text = (item.timeSlot as SpontaneousTimeSlot).auxiliaryIdentifier
        slotCode.text = (item.timeSlot as SpontaneousTimeSlot).slotCode
        startTime.text = TransformationOutput.appointmentToString((item.timeSlot as SpontaneousTimeSlot).interval.start)
        endTime.text = TransformationOutput.appointmentToString((item.timeSlot as SpontaneousTimeSlot).interval.end)
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
