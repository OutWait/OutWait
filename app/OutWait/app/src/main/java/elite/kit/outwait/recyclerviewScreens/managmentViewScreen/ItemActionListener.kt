package elite.kit.outwait.recyclerviewScreens.managmentViewScreen

interface ItemActionListener {
    //TODO generic
    fun onItemClicked(position:Int)
    fun onItemSwiped(position: Int, removedSlot: Any)
    //TODO generic
    fun editTimeSlot(position: Int)

}
