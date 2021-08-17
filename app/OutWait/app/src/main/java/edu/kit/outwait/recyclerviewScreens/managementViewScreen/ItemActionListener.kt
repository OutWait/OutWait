package edu.kit.outwait.recyclerviewScreens.managementViewScreen

import edu.kit.outwait.dataItem.TimeSlotItem
import edu.kit.outwait.waitingQueue.timeSlotModel.TimeSlot

/**
 * Maintains actions to edit recyclerview items
 *
 */
interface ItemActionListener {
    /**
     * Action click on a slot
     *
     * @param position Selected slot
     */
    fun onItemClicked(position:Int)

    /**
     * Action sipe of a slot
     *
     * @param position Swiped slot
     * @param removedSlot Slot which is swiped
     */
    fun onItemSwiped(position: Int, removedSlot: TimeSlotItem)

    /**
     * Action edit a slot
     *
     * @param position Slot which should be edit
     */
    fun editTimeSlot(position: Int)

    /**
     * Notifies to save changes
     *
     */
    fun saveTransaction()

    /**
     * Notifies to abort a transaction
     *
     */
    fun abortTransaction()
}
