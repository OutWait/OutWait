package edu.kit.outwait.dataItem

import edu.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import edu.kit.outwait.waitingQueue.timeSlotModel.Type

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
