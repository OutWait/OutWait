package elite.kit.outwait.instituteRepository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elite.kit.outwait.customDataTypes.Mode
import elite.kit.outwait.customDataTypes.Preferences
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
    private val errorNotifications = MutableLiveData<List<String>>()

    fun getObservablePreferences() = preferences as LiveData<Preferences>
    fun getObservableTimeSlotList() = timeSlotList as LiveData<List<TimeSlot>>
    fun getErrorNotifications() = errorNotifications as LiveData<List<String>>

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
        val l = listOf("Fehler")
        errorNotifications.value = l
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
