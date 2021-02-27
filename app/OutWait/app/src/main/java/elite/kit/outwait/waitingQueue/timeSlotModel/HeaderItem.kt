package elite.kit.outwait.waitingQueue.timeSlotModel

import org.joda.time.Interval

class HeaderItem(): DataItem() {
    override fun getType(): Type {
        return Type.HEADER
    }
}
