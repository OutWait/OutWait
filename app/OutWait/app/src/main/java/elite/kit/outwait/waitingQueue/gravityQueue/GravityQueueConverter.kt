package elite.kit.outwait.waitingQueue.gravityQueue

import elite.kit.outwait.customDataTypes.FixedSlot
import elite.kit.outwait.customDataTypes.ReceivedList
import elite.kit.outwait.customDataTypes.SpontaneousSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.Pause
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval

class GravityQueueConverter {

    fun receivedListToTimeSlotList(receivedList : ReceivedList, auxiliaryIdentifiers : HashMap<String, String>) : List<TimeSlot>{
        val gravityQueue = receivedListToGravityQueue(receivedList, auxiliaryIdentifiers)
        val currentSlotStartedTime = receivedList.currentSlotStartedTime
        return gravitySlotListToTimeSlotList(gravityQueue, currentSlotStartedTime, DateTime.now())
    }

    private fun receivedListToGravityQueue(receivedList: ReceivedList, auxiliaryIdentifiers: HashMap<String, String>) : List<ClientGravitySlot>{
        var gravityList = mutableListOf<ClientGravitySlot>()
        for (code in receivedList.order){
            if (isSpontaneous(code, receivedList)){
                gravityList.add(toGravitySlot(getSpontaneous(code, receivedList)!!, auxiliaryIdentifiers))
            }
            if (isFixed(code, receivedList)){
                gravityList.add(toGravitySlot(getFixed(code, receivedList)!!, auxiliaryIdentifiers))
            }
        }
        return gravityList
    }

    private fun toGravitySlot(slot: SpontaneousSlot, auxiliaryIdentifiers: HashMap<String, String>)
        = SpontaneousGravitySlot(slot.slotCode, slot.duration, getAuxiliary(slot.slotCode, auxiliaryIdentifiers))

    private fun toGravitySlot(slot: FixedSlot, auxiliaryIdentifiers: HashMap<String, String>)
        = FixedGravitySlot(slot.slotCode, slot.duration, slot.appointmentTime, getAuxiliary(slot.slotCode, auxiliaryIdentifiers))

    private fun inSpontaneous(code: String, receivedList: ReceivedList): Boolean{
        var isInList = false
        for(slot in receivedList.spontaneous){
            if(slot.slotCode == code){
                isInList = true
            }
        }
        return isInList
    }

    private fun getAuxiliary(code: String, auxiliaryIdentifiers: HashMap<String, String>): String{
        var aux = ""
        if (auxiliaryIdentifiers.containsKey(code)){
            aux = auxiliaryIdentifiers[code]!!
        }
        return aux
    }

    private fun getFixed(code: String, receivedList: ReceivedList): FixedSlot?{
        for(slot in receivedList.fixed){
            if(slot.slotCode == code){
                return slot
            }
        }
        return null
    }

    private fun getSpontaneous(code: String, receivedList: ReceivedList): SpontaneousSlot?{
        for(slot in receivedList.spontaneous){
            if(slot.slotCode == code){
                return slot
            }
        }
        return null
    }

    private fun isFixed(code: String, receivedList: ReceivedList)
        = (getFixed(code, receivedList) !== null)

    private fun isSpontaneous(code: String, receivedList: ReceivedList)
        = (getSpontaneous(code, receivedList) !== null)

    private fun gravitySlotListToTimeSlotList(gravitySlotList : List<ClientGravitySlot>, currentSlotStartedTime: DateTime, now: DateTime) : List <TimeSlot>{
        //Verspätet sich der aktuelle Slot? -> Dummy anpassen
        var firstTimeSlotDummy = Pause(Interval(currentSlotStartedTime, currentSlotStartedTime))
        if (currentSlotStartedTime + gravitySlotList[1].duration < now){
            firstTimeSlotDummy = Pause(Interval(now - gravitySlotList[1].duration, now - gravitySlotList[1].duration))
        }

        val timeSlotList = mutableListOf<TimeSlot>(firstTimeSlotDummy)

        //Gravity-Algorithmus
        for (slot in gravitySlotList){
            timeSlotList.add(slot.toClientTimeSlot(timeSlotList.last()))
        }

        //Dummy-Slot entfernen
        timeSlotList.remove(firstTimeSlotDummy)

        //Pausen zwischen auseinanderliegenden Slots einfügen
        val listWithPauses = mutableListOf<TimeSlot>()
        var i : Int = 0
        while(i < timeSlotList.size){
            listWithPauses.add(timeSlotList[i])
            if(i != timeSlotList.lastIndex && timeSlotList[i].interval.end < timeSlotList[i+1].interval.start){
                timeSlotList.add(i+1, Pause(Interval(timeSlotList[i].interval.end, timeSlotList[i+1].interval.start)))
            }
            i++
        }

        return listWithPauses
    }
}
