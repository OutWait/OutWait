package elite.kit.outwait.dataItem

import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.Type

/**
 * Represents a item of the recyclerview
 *
 */
abstract class DataItem() {
    /**
     * Gives the type of the item back
     *
     * @return Type of item
     */
    abstract fun getType(): Type
}
