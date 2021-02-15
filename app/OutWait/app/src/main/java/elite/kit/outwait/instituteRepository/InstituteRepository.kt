package elite.kit.outwait.instituteRepository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elite.kit.outwait.customDataTypes.Mode
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.waitingQueue.gravityQueue.FixedGravitySlot
import elite.kit.outwait.waitingQueue.gravityQueue.SpontaneousGravitySlot
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
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
    private val errorNotifications = MutableLiveData<List<InstituteErrors>>()
    private val inTransaction = MutableLiveData<Boolean>(false)

    fun getObservablePreferences() = preferences as LiveData<Preferences>
    fun getObservableTimeSlotList() = timeSlotList as LiveData<List<TimeSlot>>
    fun getErrorNotifications() = errorNotifications as LiveData<List<InstituteErrors>>
    fun isInTransaction() = inTransaction as LiveData<Boolean>

    suspend fun loginCo(username: String, password: String): Boolean{
        withContext(IO){
            //Server Request
            Log.d("login::InstiRepo", "before server connect running in ${Thread.currentThread().name}")
            delay(2000)
            Log.d("login::InstiRepo", "after server connect")
        }
        //change Live Data
        delay(2000)
        var d = Duration(2999999)
        Log.d("login::InstiRepo", "before liveData changed running in ${Thread.currentThread().name}")
        preferences.value = Preferences(d, d, d, d, Mode.TWO)
        Log.d("login::InstiRepo", "after liveData changed")
        val l = listOf(InstituteErrors.TRANSACTION_DENIED)
        errorNotifications.value = l

        Log.d("login::InstiRepo", "DateTime-Test-start")
        var dd = DateTime(118800000) //2.1.1970 9:00 UTC
        var slot = FixedGravitySlot("abc", Duration.standardMinutes(20), DateTime.now()-Duration.standardMinutes(10), "Hans")
        val interval = slot.interval(DateTime.now())
        val begin = interval.start
        val end = interval.end
        Log.d("login::InstiRepo", "slotBegin: ${begin.toString()}")
        Log.d("login::InstiRepo", "slotEnd: ${end.toString()}")
        Log.d("login::InstiRepo", "DateTime-Test-end")


        return true
    }

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

    fun changeSpontaneousSlotInfo(slotCode : String, duration : Duration, auxiliaryIdentifier : String){

    }

    fun moveSlotAfterAnother(movedSlot: String, otherSlot: String){

    }

    fun endCurrentSlot(){

    }

    fun deleteSlot(slotCode : String){

    }

    fun changeFixedSlotAppointmentTime(slotCode : String, duration : Duration, auxiliaryIdentifier : String ,newAppointmentTime : DateTime){

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
