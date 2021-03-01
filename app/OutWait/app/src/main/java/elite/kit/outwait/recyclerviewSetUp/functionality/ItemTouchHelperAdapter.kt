package elite.kit.outwait.recyclerviewSetUp.functionality

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition:Int,toPosition:Int)
    fun onItemSwiped(position: Int)
    fun registerMovement(newPos: Int, oldPos:Int)
}
