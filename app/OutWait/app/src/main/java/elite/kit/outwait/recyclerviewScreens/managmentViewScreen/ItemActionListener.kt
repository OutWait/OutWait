package elite.kit.outwait.recyclerviewScreens.managmentViewScreen

import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot

interface ItemActionListener {
    //TODO generic
    fun onItemClicked(position:Int)
    fun onItemSwiped(position: Int, removedSlot: TimeSlot)
    //TODO generic
    fun editTimeSlot(position: Int)

}
