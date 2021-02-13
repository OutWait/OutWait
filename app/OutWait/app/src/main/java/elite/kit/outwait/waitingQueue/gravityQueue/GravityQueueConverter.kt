package elite.kit.outwait.waitingQueue.gravityQueue

import elite.kit.outwait.waitingQueue.timeSlotModel.Pause
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval

class GravityQueueConverter {
    fun gravitySlotListToTimeSlotList(gravitySlotList : List<ClientGravitySlot>, auxiliaryIdentifiers : HashMap<String, String>, currentSlotStartedTime: DateTime, now: DateTime) : List <TimeSlot>{



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
