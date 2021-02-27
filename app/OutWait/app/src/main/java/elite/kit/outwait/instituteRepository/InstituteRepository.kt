package elite.kit.outwait.instituteRepository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elite.kit.outwait.clientRepository.ClientErrors
import elite.kit.outwait.customDataTypes.Mode
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.instituteDatabase.facade.InstituteDBFacade
import elite.kit.outwait.remoteDataSource.ManagementHandler
import elite.kit.outwait.remoteDataSource.ManagementServerErrors
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
class InstituteRepository @Inject constructor(
    private val remote: ManagementHandler,
    private val db: InstituteDBFacade
    ) {

    private val auxHelper = AuxHelper(db)

    init {
        remote.getErrors().observeForever {
            if (it.isNotEmpty()){
                when (it.last()){

                    ManagementServerErrors.LOGIN_DENIED
                        -> pushError(InstituteErrors.LOGIN_DENIED)

                    ManagementServerErrors.TRANSACTION_DENIED
                        -> doNothing()//error wird in Methode transaction() über Rückgabewert behandelt

                    else
                        -> doNothing()
                }
            }
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
        //TODO Reset Repo completely

        loggedIn.value=false
        //preferences.value = null
        //timeSlotList.value = null
        inTransaction.value = false
        //communicationEstablished = false
    }

    fun changePreferences(
        defaultSlotDuration: Duration,
        notificationTime: Duration,
        delayNotificationTime: Duration,
        prioritizationTime: Duration,
        mode2Active: Boolean
    ){
        var mode = Mode.ONE
        if (mode2Active){
            mode = Mode.TWO
        }
        val pref = Preferences(
            defaultSlotDuration,
            notificationTime,
            delayNotificationTime,
            prioritizationTime,
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
        CoroutineScope(IO).launch {
            if (transaction()){
                remote.moveSlotAfterAnother(movedSlot, otherSlot)
            }
        }
    }

    fun endCurrentSlot(){
        CoroutineScope(IO).launch {
            if (transaction()){
                remote.endCurrentSlot()
            }
        }
    }

    fun deleteSlot(slotCode : String){
        CoroutineScope(IO).launch {
            if (transaction()){
                remote.deleteSlot(slotCode)
            }
        }
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
            pushError(InstituteErrors.NOT_IN_TRANSACTION)
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
            pushError(InstituteErrors.NOT_IN_TRANSACTION)
        }
    }

    fun passwordForgotten(username : String){
        CoroutineScope(IO).launch {
            remote.resetPassword(username)
        }
    }

    private fun doNothing(){
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
