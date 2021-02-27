package elite.kit.outwait.dataItem

import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.Type

class TimeSlotItem(private var timeSlot:TimeSlot): DataItem() {
    override fun getType(): Type {
        return timeSlot.getType()
    }

}
