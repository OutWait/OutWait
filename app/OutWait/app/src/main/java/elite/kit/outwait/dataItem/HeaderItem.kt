package elite.kit.outwait.dataItem

import elite.kit.outwait.waitingQueue.timeSlotModel.Type

class HeaderItem(): DataItem(){
    override fun getType(): Type {
        return Type.HEADER
    }
}
