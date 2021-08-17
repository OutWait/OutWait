package edu.kit.outwait.recyclerviewSetUp.viewHolder

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import edu.kit.outwait.R
import edu.kit.outwait.dataItem.TimeSlotItem
import edu.kit.outwait.recyclerviewScreens.managementViewScreen.ItemActionListener
import edu.kit.outwait.utils.TransformationOutput
import edu.kit.outwait.waitingQueue.timeSlotModel.SpontaneousTimeSlot
import edu.kit.outwait.waitingQueue.timeSlotModel.TimeSlot

/**
 * ViewHolder for a spontaneous slot
 *
 * @property itemViewSpo View of a spontaneous slot
 * @property itemTouchHelper Instance to add function on recyclerview
 * @constructor
 * Add listeners on view
 *
 * @param listener ManagementViewFragment
 */
class SpontaneousSlotViewHolder(
    private var itemViewSpo: View,
    private var itemTouchHelper: ItemTouchHelper,
    listener: ItemActionListener
) :
    BaseViewHolder<SpontaneousTimeSlot>(itemViewSpo), View.OnTouchListener,
    GestureDetector.OnGestureListener {
    private var identifier = itemViewSpo.findViewById<TextView>(R.id.tvIdentifierSpo)
    private var slotCode = itemViewSpo.findViewById<TextView>(R.id.tvSlotCodeSpo)
    private var startTime = itemViewSpo.findViewById<TextView>(R.id.tvStartTimeSpo)
    private var endTime = itemViewSpo.findViewById<TextView>(R.id.tvEndTimeSpo)
    private var icon = itemViewSpo.findViewById<ImageView>(R.id.ivEditIconSpo)
    private var container = itemViewSpo.findViewById<ConstraintLayout>(R.id.tvSpoSlotContainerSpo)

    private val gestureDetector: GestureDetector = GestureDetector(itemViewSpo.context, this)

    init {
        itemViewSpo.setOnTouchListener(this)

        container.setOnClickListener {
            if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                listener.onItemClicked(absoluteAdapterPosition)
            }
        }
        icon.setOnClickListener {
            if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                listener.editTimeSlot(absoluteAdapterPosition)
            }
        }
    }

    /**
     * Binds attributes of item with its layout
     *
     * @param item Spontaneous slot
     */
    override fun bind(item: TimeSlotItem) {
        identifier.text = (item.timeSlot as SpontaneousTimeSlot).auxiliaryIdentifier
        slotCode.text = (item.timeSlot as SpontaneousTimeSlot).slotCode
        startTime.text =
            TransformationOutput.appointmentToString((item.timeSlot as SpontaneousTimeSlot).interval.start)
        endTime.text =
            TransformationOutput.appointmentToString((item.timeSlot as SpontaneousTimeSlot).interval.end)
    }

    /**
     * Called when a touch event is dispatched to a view.
     *
     * @param v Touched View
     * @param event Object used to report movement (mouse, pen, finger, trackball) events
     * @return Always true
     */
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    /**
     * Notified when a tap occurs with the down MotionEvent that triggered it.
     *
     * @param e Object used to report movement (mouse, pen, finger, trackball) events
     * @return Always true
     */
    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    /**
     * The user has performed a down MotionEvent and not performed a move or up yet.
     *
     * @param e Object used to report movement (mouse, pen, finger, trackball) events
     */
    override fun onShowPress(e: MotionEvent?) {
    }

    /**
     * The user has performed a short tap
     *
     * @param e Object used to report movement (mouse, pen, finger, trackball) events
     * @return Always true
     */
    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return true
    }


    /**
     * The user has performed a scroll
     *
     * @param e1 The first down motion event that started the scrolling.
     * @param e2 The move motion event that triggered the current onScroll.
     * @param distanceX The distance along the X axis that has been scrolled since the last call to onScroll.
     * @param distanceY  The distance along the Y axis that has been scrolled since the last call to onScroll.
     * @return Always true
     */
    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float,
    ): Boolean {
        return true
    }

    /**
     * The user has performed a long press on the view
     *
     * @param e Object used to report movement (mouse, pen, finger, trackball) events
     */
    override fun onLongPress(e: MotionEvent?) {
        itemTouchHelper.startDrag(this)
    }

    /**
     * Notified of a fling event when it occurs with the initial on down MotionEvent and the matching up MotionEvent.
     *
     * @param e1 The first down motion event that started the scrolling.
     * @param e2 The move motion event that triggered the current onScroll.
     * @param velocityX Velocity in x pixel per seconds
     * @param velocityY Velocity in y pixel per seconds
     * @return Always true
     */
    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float,
    ): Boolean {
        return true
    }
}
