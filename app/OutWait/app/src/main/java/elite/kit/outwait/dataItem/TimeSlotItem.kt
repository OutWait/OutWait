package elite.kit.outwait.dataItem

import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.Type

/**
 * Encapsulates a timeslot in order to be an item in te recyclrview
 *
 * @property timeSlot A Slot of pause, spontaneous or fixed
 */
class TimeSlotItem( var timeSlot:TimeSlot): DataItem() {
    /**
     * Gives the type back
     *
     * @return Type of pause, spontaneous oder fixed
     */
    override fun getType(): Type {
        return timeSlot.getType()
    }

}
