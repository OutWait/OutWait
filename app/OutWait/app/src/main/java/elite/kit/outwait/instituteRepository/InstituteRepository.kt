package elite.kit.outwait.instituteRepository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elite.kit.outwait.clientRepository.ClientErrors
import elite.kit.outwait.customDataTypes.Mode
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.remoteDataSource.ManagementHandler
import elite.kit.outwait.waitingQueue.gravityQueue.FixedGravitySlot
import elite.kit.outwait.waitingQueue.gravityQueue.GravityQueueConverter
import elite.kit.outwait.waitingQueue.gravityQueue.SpontaneousGravitySlot
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.Duration
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class InstituteRepository @Inject constructor(private val remote: ManagementHandler) {




    init {
        remote.getErrors().observeForever {


        }
    }



    private val preferences = MutableLiveData<Preferences>()
    private val timeSlotList = MutableLiveData<List<TimeSlot>>()
    private val errorNotifications = MutableLiveData<List<InstituteErrors>>()
    private val inTransaction = MutableLiveData<Boolean>(false)
    private val loggedIn = MutableLiveData<Boolean>(false)


    fun getObservablePreferences() = preferences as LiveData<Preferences>
    fun getObservableTimeSlotList() = timeSlotList as LiveData<List<TimeSlot>>
    fun getErrorNotifications() = errorNotifications as LiveData<List<InstituteErrors>>
    fun isInTransaction() = inTransaction as LiveData<Boolean>
    fun isLoggedIn() = loggedIn as LiveData<Boolean>

    init {
        preferences.value=Preferences(Duration(300000L),Duration(3000L),Duration(3000L),Duration(3000L),Mode.ONE)

    }


    suspend fun loginCo(username: String, password: String): Boolean {
        withContext(IO) {
            //Server Request
            Log.d("login::InstiRepo",
                "before server connect running in ${Thread.currentThread().name}")
            delay(2000)
            Log.d("login::InstiRepo", "after server connect")
        }
        //change Live Data
        delay(2000)
        var d = Duration(2999999)
        Log.d("login::InstiRepo",
            "before liveData changed running in ${Thread.currentThread().name}")
        preferences.value = Preferences(d, d, d, d, Mode.TWO)
        Log.d("login::InstiRepo", "after liveData changed")
        val l = listOf(InstituteErrors.TRANSACTION_DENIED)
        errorNotifications.value = l
        return true
    }


    private var communicationEstablished = false

    fun login(username: String, password: String){
        CoroutineScope(IO).launch {
            if(communicationEstablished || remote.initCommunication()){
                if(remote.login(username, password)){
                    observeRemote()
                    loggedIn.postValue(true)
                }
            }
        }
    }

    private suspend fun observeRemote(){
        withContext(Main){
            remote.getReceivedList().observeForever {
                Log.d("InstiRepo", "receivedList empfangen")
                val converter = GravityQueueConverter()
                val timeSlots = converter.receivedListToTimeSlotList(it, HashMap<String, String>())
                timeSlotList.value = timeSlots
            }
            remote.getUpdatedPreferences().observeForever {
                preferences.value = it
            }
        }
    }

    fun logout(){
        CoroutineScope(IO).launch {
            remote.logout()
        }
        loggedIn.value=false
    }

    fun changePreferences(
        defaultSlotDurations: Duration,
        notificationTimes: Duration,
        delayNotificationTimes: Duration,
        prioritizationTimes: Duration,
        mode2Actives: Boolean
    ){
       preferences.value!!.mode=Mode.TWO
        preferences.value!!.defaultSlotDuration=defaultSlotDurations
        Log.i("change","${preferences.value!!.defaultSlotDuration.millis}")

        var mode = Mode.ONE
        if (mode2Actives){
            mode = Mode.TWO
        }
        val pref = Preferences(
            defaultSlotDurations,
            notificationTimes,
            delayNotificationTimes,
            prioritizationTimes,
            mode
        )
        CoroutineScope(IO).launch{
            remote.changePreferences(pref)
        }
    }

    fun newSpontaneousSlot(auxiliaryIdentifier : String, duration : Duration){
        //add aux to DB
        CoroutineScope(IO).launch {
            if (transaction()){
                remote.addSpontaneousSlot(duration, DateTime.now())
            }
        }
    }

    fun newFixedSlot(auxiliaryIdentifier : String, appointmentTime : DateTime, duration : Duration){
        //add aux to db
        CoroutineScope(IO).launch {
            if(transaction()){
                remote.addFixedSlot(duration, appointmentTime)
            }
        }
    }

    fun changeSpontaneousSlotInfo(slotCode : String, duration : Duration, auxiliaryIdentifier : String){
        //change aux
        CoroutineScope(IO).launch {
            if (transaction()){
                remote.changeSlotDuration(slotCode, duration)
            }
        }
    }

    fun moveSlotAfterAnother(movedSlot: String, otherSlot: String){

    }

    fun endCurrentSlot(){

    }

    fun deleteSlot(slotCode : String){

    }

    fun changeFixedSlotInfo(slotCode : String, duration : Duration, auxiliaryIdentifier : String ,newAppointmentTime : DateTime){
        CoroutineScope(IO).launch {
            if (transaction()){
                remote.changeFixedSlotTime(slotCode, newAppointmentTime)
                remote.changeSlotDuration(slotCode, duration)
            }
        }
    }

    fun saveTransaction(){
        if (inTransaction.value == true){
            CoroutineScope(IO).launch {
                remote.saveTransaction()
            }
            inTransaction.value = false
        }
        else{
            //keine transaktion zu speichern
        }
    }

    fun abortTransaction(){
        if (inTransaction.value == true){
            CoroutineScope(IO).launch {
                remote.abortTransaction()
            }
            inTransaction.value = false
        }
        else{
            //keine transaktion zu speichern
        }
    }

    fun passwordForgotten(username : String){

    }

    fun doSomething(){
        Log.d("InstituteRepo", "method DoSomething is reached in FR2")
    }

    private suspend fun transaction(): Boolean{
        if (inTransaction.value == true){
            return true
        } else{
            val transactionEstablished = remote.startTransaction()
            if (transactionEstablished){
                inTransaction.postValue(true)
                return true
            }
            pushError(InstituteErrors.TRANSACTION_DENIED)
            return false
        }
    }
    private fun pushError(error: InstituteErrors){
        if (errorNotifications.value !== null){
            val newList = errorNotifications.value!!.plus(error).toMutableList()
            errorNotifications.postValue(newList)
        }else{
            errorNotifications.postValue(listOf(error))
        }
    }
}
