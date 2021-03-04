package elite.kit.outwait.dataItem

import elite.kit.outwait.waitingQueue.timeSlotModel.Type

/**
 * Rpresents the header of the transaction
 *
 */
class HeaderItem(): DataItem(){
    /**
     * Gives the type back
     *
     * @return Header type
     */
    override fun getType(): Type {
        return Type.HEADER
    }
}
