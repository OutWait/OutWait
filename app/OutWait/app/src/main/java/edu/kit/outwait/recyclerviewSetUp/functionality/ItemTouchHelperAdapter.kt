package edu.kit.outwait.recyclerviewSetUp.functionality

/**
 * Register actions on the recyclerview
 *
 */
interface ItemTouchHelperAdapter {
    /**
     * Slots has moved in the recyclerview
     *
     * @param fromPosition Drag position
     * @param toPosition Target Position
     */
    fun onItemMove(fromPosition:Int,toPosition:Int)

    /**
     * Slots has swiped in the recyclerview
     *
     * @param position Position of swiped slot
     */
    fun onItemSwiped(position: Int)

    /**
     * Registers movements in the recylcerview
     *
     * @param newPos Drag position
     * @param oldPos Target Position
     */
    fun registerMovement(newPos: Int, oldPos:Int)
}
