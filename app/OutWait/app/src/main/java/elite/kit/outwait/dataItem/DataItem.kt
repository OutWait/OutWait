package elite.kit.outwait.dataItem

import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.Type

abstract class DataItem() {
abstract fun getType(): Type
}
