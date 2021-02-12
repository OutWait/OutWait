package elite.kit.outwait.waitingQueue.gravityQueue

import elite.kit.outwait.waitingQueue.timeSlotModel.Pause
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import org.joda.time.DateTime
import org.joda.time.Interval

class GravityQueueConverter {
    fun gravitySlotListToTimeSlotList(gravitySlotList : List<ClientGravitySlot>, auxiliaryIdentifiers : HashMap<String, String>, currentSlotStartedTime: DateTime, now: DateTime) : List <TimeSlot>{
        val timeSlotList = listOf<TimeSlot>()

        //Verspätet sich der aktuelle Slot? -> Dummy anpassen
        var firstTimeSlotDummy = Pause(Interval(currentSlotStartedTime, currentSlotStartedTime))
        if (currentSlotStartedTime + gravitySlotList[1].duration < now){
            firstTimeSlotDummy = Pause(Interval(now - gravitySlotList[1].duration, now - gravitySlotList[1].duration))
        }

        //Gravity-Algorithmus
        for (slot in gravitySlotList){
            timeSlotList.plus(slot.toClientTimeSlot(timeSlotList.last()))
        }

        //Dummy-Slot entfernen
        timeSlotList.minus(firstTimeSlotDummy)

        val listWithPauses = listOf<TimeSlot>()
        /*
        for (i in 0..timeSlotList.lastIndex){
            listWithPauses.plus(timeSlotList[i])
            if(i != timeSlotList.lastIndex && ){

            }
        }*/

        return timeSlotList
    }
}
