package elite.kit.outwait.instituteRepository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elite.kit.outwait.customDataTypes.Mode
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import org.joda.time.DateTime
import org.joda.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstituteRepository @Inject constructor() {

    init {

    }

    private val preferences = MutableLiveData<Preferences>()
    private val timeSlotList = MutableLiveData<List<TimeSlot>>()
    private val errorNotifications = MutableLiveData<List<String>>()

    fun getObservablePreferences() = preferences as LiveData<Preferences>
    fun getObservableTimeSlotList() = timeSlotList as LiveData<List<TimeSlot>>
    fun getErrorNotifications() = errorNotifications as LiveData<List<String>>

    fun login(username: String, password: String){

    }

    fun logout(){

    }

    fun changePreferences(
        defaultSlotDuration: Duration,
        notificationTime: Duration,
        delayNotificationTime: Duration,
        prioritizationTime: Duration,
        mode2Active: Boolean
    ){

    }

    fun newSpontaneousSlot(auxiliaryIdentifier : String, duration : Duration){

    }

    fun newFixedSlot(auxiliaryIdentifier : String, appointmentTime : DateTime, duration : Duration){

    }

    fun changeSlotInfo(slotCode : String, duration : Duration, auxiliaryIdentifier : String){

    }

    fun moveSlotAfterAnother(movedSlot: String, otherSlot: String){

    }

    fun endCurrentSlot(){

    }

    fun deleteSlot(slotCode : String){

    }

    fun changeFixedSlotAppointmentTime(slotCode : String, newAppointmentTime : DateTime){

    }

    fun saveTransaction(){

    }

    fun abortTransaction(){

    }

    fun passwordForgotten(username : String){

    }

    fun doSomething(){
        Log.d("InstituteRepo", "method DoSomething is reached in FR2")
    }
}
