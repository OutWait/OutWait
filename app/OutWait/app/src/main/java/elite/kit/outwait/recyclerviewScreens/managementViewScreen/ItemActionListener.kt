package elite.kit.outwait.recyclerviewScreens.managementViewScreen

import elite.kit.outwait.dataItem.TimeSlotItem
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot

interface ItemActionListener {
    fun onItemClicked(position:Int)
    fun onItemSwiped(position: Int, removedSlot: TimeSlotItem)
    fun editTimeSlot(position: Int)
    fun saveTransaction()
    fun abortTransaction()
}
