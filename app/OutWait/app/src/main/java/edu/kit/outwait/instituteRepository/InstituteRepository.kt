package edu.kit.outwait.instituteRepository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import edu.kit.outwait.customDataTypes.Preferences
import edu.kit.outwait.customDataTypes.ReceivedList
import edu.kit.outwait.instituteDatabase.facade.InstituteDBFacade
import edu.kit.outwait.remoteDataSource.ManagementHandler
import edu.kit.outwait.remoteDataSource.ManagementServerErrors
import edu.kit.outwait.utils.EspressoIdlingResource
import edu.kit.outwait.utils.EspressoIdlingResource.wrapEspressoIdlingResource
import edu.kit.outwait.waitingQueue.gravityQueue.GravityQueueConverter
import edu.kit.outwait.waitingQueue.timeSlotModel.TimeSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class is the single source of truth for all institute-related information,
 * especially the waiting queue. The main functionality is delegating manipulation
 * requests for the waiting queue to the server (trough the remoteDataSource) and
 * preprocessing data sent by the server before providing it to the GUI through live
 * data. All public methods return immediately and do their work in coroutines.
 * Their success always results in a change of the Live Data it provides through
 * [getObservablePreferences], [getObservableTimeSlotList], [isLoggedIn],
 * [isInTransaction]. E.g., after all successful queue manipulations,
 * [getObservableTimeSlotList] is set to a new value.
 * When a Method or a request is not successful, an [InstituteErrors] is pushed
 * into the list provided by [getErrorNotifications]
 *
 * @property remote object to send instructions to the server and to receive its requests
 * @property db facade to access the institutes local database
 */
@Singleton
class InstituteRepository @Inject constructor(
    private val remote: ManagementHandler,
    private val db: InstituteDBFacade
) {
    //The repositories [AuxHelper]. See documentation of [AuxHelper] class
    private val auxHelper = AuxHelper(db)


    init {
        /*
        How to react to all kinds of errors that can happen in the
        communication with the server
         */
        remote.getErrors().observeForever {
            if (it.isNotEmpty()) {
                when (it.last()) {
                    ManagementServerErrors.LOGIN_DENIED
                    -> pushError(InstituteErrors.LOGIN_DENIED)
                    ManagementServerErrors.TRANSACTION_DENIED
                    -> pushError(InstituteErrors.TRANSACTION_DENIED)
                    ManagementServerErrors.NETWORK_ERROR
                    -> {
                        cleanUp()
                        pushError(InstituteErrors.NETWORK_ERROR)
                    }
                    ManagementServerErrors.SERVER_DID_NOT_RESPOND
                    -> {
                        cleanUp()
                        pushError(InstituteErrors.COMMUNICATION_ERROR)
                    }
                    ManagementServerErrors.COULD_NOT_CONNECT
                    -> {
                        cleanUp()
                        pushError(InstituteErrors.COMMUNICATION_ERROR)
                    }
                    ManagementServerErrors.INVALID_REQUEST
                    -> {
                        logout(false)
                        pushError(InstituteErrors.INVALID_REQUEST)
                    }
                    ManagementServerErrors.INTERNAL_SERVER_ERROR
                    -> {
                        logout(false)
                        pushError(InstituteErrors.SERVER_ERROR)
                    }
                }
            }
        }
        /*
        Receiving new preferences or new waiting queue from the server
         */
        remote.getReceivedList().observeForever {
            if (it !== null) receivedNewList(it)
        }
        remote.getUpdatedPreferences().observeForever {
            if (it !== null) preferences.value = it
            Log.i("instiRepo", "preferences received")
        }

        CoroutineScope(IO).launch {
            if (db.loginDataSaved()) {
                val password = db.getPassword()
                val username = db.getUserName()
                loginData.postValue(Pair(username, password))
            }
        }
    }


    private val preferences = MutableLiveData<Preferences>()
    private val timeSlotList = MutableLiveData<List<TimeSlot>>()
    private val errorNotifications = MutableLiveData<List<InstituteErrors>>()
    private val inTransaction = MutableLiveData<Boolean>(false)
    private val loggedIn = MutableLiveData<Boolean>(false)
    private val loginData = MutableLiveData<Pair<String, String>>(Pair("", ""))

    /** Provides an observable object that stores all institute preferences */
    fun getObservablePreferences() = preferences as LiveData<Preferences>

    /**
     * Provides an observable List that stores the waiting queue. All slots
     * are ordered by their time of beginning and contain all slot specific
     * information. Further more, if there is free time between two client slots,
     * there is a pause slot in between them.
     */
    fun getObservableTimeSlotList() = timeSlotList as LiveData<List<TimeSlot>>

    /**
     * Returns all pushed error notifications in an observable list,
     * sorted chronologically by their point of occurrence. Those errors
     * can happen due to communication problems, server problems or usage
     * problems (e.g. two receptionists trying to do a transaction at
     * the same time). See [InstituteErrors]
     */
    fun getErrorNotifications() = errorNotifications as LiveData<List<InstituteErrors>>

    /** Provides an observable boolean that tells if there is an opened queue manipulation
     * transaction on this device
     */
    fun isInTransaction() = inTransaction as LiveData<Boolean>

    /** Provides an observable boolean that tells if the institute is logged in*/
    fun isLoggedIn() = loggedIn as LiveData<Boolean>

    /** Provides observable Pair with first username and second password */
    fun getLoginData(): LiveData<Pair<String, String>> = loginData


    private var communicationEstablished = false

    /**
     * Tries to log in the institute with its [username] and [password]
     * If the server accepts the login, [isLoggedIn] will be set true.
     * [getObservableTimeSlotList] and [getObservablePreferences] will change
     * also because the server sends this information after login.
     * In case the login does not succeed, an error will be pushed (see [getErrorNotifications])
     * This method always returns immediately.
     *
     * @param username username of the institute
     * @param password password of the institute
     */
    fun login(username: String, password: String) {
        EspressoIdlingResource.increment()
        CoroutineScope(IO).launch {
            if (communicationEstablished || remote.initCommunication()) {
                if (remote.login(username, password)) {
                    communicationEstablished = true
                    loggedIn.postValue(true)
                    loginData.postValue(Pair(username, password))
                    db.insertUpdateLoginData(username, password)
                }
                EspressoIdlingResource.decrement()
            }
        }
    }

    /*
    When a new waiting queue is received from the server, we first have to
    delegate it to the aux helper to possibly match a new auxiliary identifier
    with a new slot code or delete obsolete auxiliary identifiers. See
    Second, we run the gravity algorithm which builds the time slot list
    that we can provide to the GUI.
     */
    private fun receivedNewList(receivedList: ReceivedList) {
        CoroutineScope(IO).launch {
            Log.d("InstiRepo", "receivedList empfangen")
            val newAuxMap = auxHelper.receivedList(
                receivedList,
                inTransaction.value!!
            ) //we never set inTransaction null, so we can assure it has a non null value
            val timeSlots = GravityQueueConverter().receivedListToTimeSlotList(
                receivedList,
                newAuxMap
            )
            timeSlotList.postValue(timeSlots)
        }

    }

    /**
     * Sends a logout request to the server.
     *
     */
    fun logout() = logout(true)

    fun logout(manual: Boolean) {
        EspressoIdlingResource.increment()

        CoroutineScope(IO).launch {
            remote.logout()
            if (manual) {
                db.deleteAll()
                loginData.postValue(Pair("", ""))
            }
            EspressoIdlingResource.decrement()
        }
        cleanUp()
    }

    /**
     * Sends new institute preferences to the server
     *
     * @param preferences see [Preferences]
     */
    fun changePreferences(preferences: Preferences) {
        EspressoIdlingResource.increment()
        CoroutineScope(IO).launch {
            remote.changePreferences(preferences)
            EspressoIdlingResource.decrement()
        }
    }

    /**
     * Requests the server to create a new spontaneous slot
     *
     * @param auxiliaryIdentifier entered by the receptionist. helps to remember
     * the name of the client and/or details about the appointment
     * @param duration how much time is scheduled for the slot.
     */
    fun newSpontaneousSlot(auxiliaryIdentifier: String, duration: Duration) {
        EspressoIdlingResource.increment()
        CoroutineScope(IO).launch {
            if (transaction()) {
                auxHelper.newAux(auxiliaryIdentifier)
                remote.addSpontaneousSlot(duration, DateTime.now())
            }
            EspressoIdlingResource.decrement()
        }
    }

    /**
     * Requests the server to create a new slot for a client who has a fixed appointment
     *
     * @param auxiliaryIdentifier entered by the receptionist. helps to remember
     * the name of the client and/or details about the appointment
     * @param appointmentTime with the client arranged point in time of the appointment
     * @param duration how much time is scheduled for the slot.
     */
    fun newFixedSlot(auxiliaryIdentifier: String, appointmentTime: DateTime, duration: Duration) {
        EspressoIdlingResource.increment()
        //add aux to db
        CoroutineScope(IO).launch {
            if (transaction()) {
                auxHelper.newAux(auxiliaryIdentifier)
                remote.addFixedSlot(duration, appointmentTime)
            }
            EspressoIdlingResource.decrement()
        }
    }

    /**
     * Changes the auxiliary identifier and requests the server to change the
     * duration of the spontaneous slot to the given value
     *
     * @param slotCode the unique code, generated by the server, to identify the slot
     * @param duration how much time is scheduled for the slot
     * @param auxiliaryIdentifier entered by the receptionist. helps to remember
     * the name of the client and/or details about the appointment
     */
    fun changeSpontaneousSlotInfo(
        slotCode: String,
        duration: Duration,
        auxiliaryIdentifier: String
    ) {
        EspressoIdlingResource.increment()
        CoroutineScope(IO).launch {
            if (transaction()) {
                auxHelper.changeAux(slotCode, auxiliaryIdentifier)
                remote.changeSlotDuration(slotCode, duration)
            }
            EspressoIdlingResource.decrement()
        }
    }

    /**
     * Changes the auxiliary identifier and requests the server to change the
     * duration and the appointment time of the fixed slot to the given values
     *
     * @param slotCode the unique code, generated by the server, to identify the slot
     * @param duration how much time is scheduled for the slot
     * @param auxiliaryIdentifier entered by the receptionist. helps to remember
     * the name of the client and/or details about the appointment
     * @param newAppointmentTime the new, with the client arranged point in time of the appointment
     */
    fun changeFixedSlotInfo(
        slotCode: String,
        duration: Duration,
        auxiliaryIdentifier: String,
        newAppointmentTime: DateTime
    ) {
        EspressoIdlingResource.increment()
        CoroutineScope(IO).launch {
            if (transaction()) {
                auxHelper.changeAux(slotCode, auxiliaryIdentifier)
                remote.changeFixedSlotTime(slotCode, newAppointmentTime)
                remote.changeSlotDuration(slotCode, duration)
            }
            EspressoIdlingResource.decrement()
        }
    }

    /**
     * Requests the server to move the [movedSlot] after the [otherSlot].
     * Like specified in F10 and F11 of the functional specifications, this
     * operation is not always legal. In this case, the server does not change
     * the queue
     *
     * @param movedSlot the slot code of the slot which shall be moved after the [otherSlot]
     * @param otherSlot the slot code of the slot that shall now
     * be the predecessor of the [movedSlot]
     */
    fun moveSlotAfterAnother(movedSlot: String, otherSlot: String) {
        EspressoIdlingResource.increment()
        CoroutineScope(IO).launch {
            if (transaction()) {
                remote.moveSlotAfterAnother(movedSlot, otherSlot)
            }
            EspressoIdlingResource.decrement()
        }
    }

    /**
     * Requests the server to end the current slot. Shall be used
     * usually when the appointment of the current client is finished.
     *
     */
    fun endCurrentSlot() {
        EspressoIdlingResource.increment()
        CoroutineScope(IO).launch {
            if (transaction()) {
                remote.endCurrentSlot()
            }
            EspressoIdlingResource.decrement()
        }

    }

    /**
     * Requests the server to delete the slot with the given slot code
     * from the waiting queue
     *
     * @param slotCode the unique code, generated by the server, to identify the slot
     */
    fun deleteSlot(slotCode: String) {
        EspressoIdlingResource.increment()
        CoroutineScope(IO).launch {
            if (transaction()) {
                remote.deleteSlot(slotCode)
                EspressoIdlingResource.decrement()
            }
        }
    }

    /**
     * Tells the server that we want to save the changes done in the methods
     * [newSpontaneousSlot], [newFixedSlot], [changeSpontaneousSlotInfo],
     * [changeFixedSlotInfo], [moveSlotAfterAnother], [endCurrentSlot] and
     * [deleteSlot]
     *
     */
    fun saveTransaction() {
        EspressoIdlingResource.increment()
        if (inTransaction.value == true) {
            inTransaction.value = false
            CoroutineScope(IO).launch {
                remote.saveTransaction()
                EspressoIdlingResource.decrement()
            }

        } else {
            pushError(InstituteErrors.NOT_IN_TRANSACTION)
        }
    }

    /**
     * Tells the server that we don´t want to save the changes done in the methods
     * [newSpontaneousSlot], [newFixedSlot], [changeSpontaneousSlotInfo],
     * [changeFixedSlotInfo], [moveSlotAfterAnother], [endCurrentSlot] and
     * [deleteSlot]
     *
     */
    fun abortTransaction() {
        EspressoIdlingResource.increment()

        if (inTransaction.value == true) {
            inTransaction.value = false
            CoroutineScope(IO).launch {
                    remote.abortTransaction()
                EspressoIdlingResource.decrement()
            }
        } else {
            pushError(InstituteErrors.NOT_IN_TRANSACTION)
        }
    }

    /**
     * Tells the server that the institution has forgotten its password
     *
     * @param username username of the institution
     */
    fun passwordForgotten(username: String) {
        EspressoIdlingResource.increment()
        CoroutineScope(IO).launch {
                if (!communicationEstablished) remote.initCommunication()
                remote.resetPassword(username)
                remote.endCommunication()
            EspressoIdlingResource.decrement()
        }
    }

    /*
    If a transaction is already running, this method returns true immediately.
    Elsewise, it requests the server to start a transaction and returns true if
    this is successful and false if not. If necessary, the method updates the value
    of [inTransaction]
     */
    private fun transaction(): Boolean {
        if (inTransaction.value == true) {
            return true
        } else {
            val transactionEstablished = remote.startTransaction()
            if (transactionEstablished) {
                inTransaction.postValue(true)
                return true
            } else {
                inTransaction.postValue(false)
            }
            //errorNotification is pushed as reaction to an remote data source error
            return false
        }
    }

    /*
    Adds new error to the end of the errorNotifications list and triggers
    it to inform its observers. Can be called from coroutines (uses postValue)
     */
    private fun pushError(error: InstituteErrors) {
        if (errorNotifications.value !== null) {
            val newList = errorNotifications.value!!.plus(error).toMutableList()
            errorNotifications.postValue(newList)
        } else {
            errorNotifications.postValue(listOf(error))
        }
    }

    /*
    Resets the repository after logout or system error
     */
    private fun cleanUp() {
        timeSlotList.value = listOf()
        loggedIn.value = false
        communicationEstablished = false
        inTransaction.value = false
    }
}
