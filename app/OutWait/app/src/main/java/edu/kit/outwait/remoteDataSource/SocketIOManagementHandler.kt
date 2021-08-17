package edu.kit.outwait.remoteDataSource

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import edu.kit.outwait.customDataTypes.Preferences
import edu.kit.outwait.customDataTypes.ReceivedList
import edu.kit.outwait.networkProtocol.*
import org.joda.time.DateTime
import org.joda.time.Duration

/**
 * Defines the maximum amount of time waited for an awaited response of the server
 */
private const val MAX_TIMEWAIT_FOR_RESPONSE = 3000L

/**
 * Defines the sampling granulation with which received server response is checked
 */
private const val TIME_STEP_FOR_RESPONSE_WAIT = 100L

/**
 * The namespace for the management connection
 */
private const val namespaceManagement: String = "/management"

/**
 * This class represents the "concrete product" of ManagementHandler
 * in the, here used and commonly known as, "abstract factory pattern".
 * It implements all methods for network communication, that the
 * institute repository (or higher tier) can use to send to and receive data from the server,
 * using the implementation of a web socket connection.
 *
 */
class SocketIOManagementHandler : ManagementHandler {

    /** Internal state variable for login state of the ManagementHandler */
    private var loginRequested = false

    /** Internal state variable for login state of the ManagementHandler */
    private var loggedIn = false

    /** Internal state variable for login state of the ManagementHandler */
    private var loginDenied = false

    /** Internal state variable for transaction state of the ManagementHandler */
    private var transactionStarted = false

    /** Internal state variable for transaction state of the ManagementHandler */
    private var transactionDenied = false

    /**
     * The pushed error messages of type ManagementServerErrors as observable LiveData
     * (encapsulated with backing property)
     */
    private val _errors = MutableLiveData<List<ManagementServerErrors>>()
    override fun getErrors() = _errors as LiveData<List<ManagementServerErrors>>

    /**
     * The current waiting queue of type ReceivedList as observable LiveData
     * (encapsulated with backing property)
     */
    private val _currentList = MutableLiveData<ReceivedList>(null)
    private val currentList: LiveData<ReceivedList>
        get() = _currentList


    /**
     * The current management settings of type Preferences as observable LiveData
     * (encapsulated with backing property)
     */
    private val _currentPrefs = MutableLiveData<Preferences>(null)
    private val currentPrefs: LiveData<Preferences>
        get() = _currentPrefs

    /**
     * The mapping of events to their respective callback methods
     */
    private val managementEventToCallbackMapping: HashMap<Event,
            (wrappedJSONData: JSONObjectWrapper) -> Unit> = hashMapOf()

    /**
     * The underlying SocketAdapter which holds the connection to the server using
     * web sockets
     */
    private val mSocket: SocketAdapter


    init {
        mSocket = SocketAdapter(namespaceManagement)

        // configure HashMap that maps receiving events to callbacks
        managementEventToCallbackMapping[Event.TRANSACTION_STARTED_M] = { receivedData ->
            onTransactionStarted(receivedData as JSONEmptyWrapper)
        }
        managementEventToCallbackMapping[Event.TRANSACTION_DENIED_M] = { receivedData ->
            onTransactionDenied(receivedData as JSONEmptyWrapper)
        }
        managementEventToCallbackMapping[Event.LOGIN_REQUEST_M] = { receivedData ->
            onLoginRequest(receivedData as JSONEmptyWrapper)
        }
        managementEventToCallbackMapping[Event.INVALID_REQUEST_M] = { receivedData ->
            onInvalidRequest(receivedData as JSONErrorMessageWrapper)
        }
        managementEventToCallbackMapping[Event.INTERNAL_SERVER_ERROR_M] = { receivedData ->
            onInternalServerError(receivedData as JSONErrorMessageWrapper)
        }
        managementEventToCallbackMapping[Event.MANAGEMENT_LOGIN_SUCCESS_M] = { receivedData ->
            onLoginSuccess(receivedData as JSONEmptyWrapper)
        }
        managementEventToCallbackMapping[Event.MANAGEMENT_LOGIN_DENIED_M] = { receivedData ->
            onLoginDenied(receivedData as JSONEmptyWrapper)
        }
        managementEventToCallbackMapping[Event.UPDATE_MANAGEMENT_SETTINGS_M] = { receivedData ->
            onUpdateManagementSettings(receivedData as JSONManagementSettingsWrapper)
        }
        managementEventToCallbackMapping[Event.UPDATE_QUEUE_M] = { receivedData ->
            onUpdateQueue(receivedData as JSONQueueWrapper)
        }
        managementEventToCallbackMapping[Event.NETWORK_ERROR] = { receivedData ->
            onNetworkError(receivedData as JSONEmptyWrapper)
        }
    }

    /**
     * This method initiates the communication with the server
     *
     * @return True if the communication was successfully established, else false
     */
    override fun initCommunication(): Boolean {
        if (!mSocket.initializeConnection(managementEventToCallbackMapping)) {
            pushError(ManagementServerErrors.COULD_NOT_CONNECT)
            endCommunication()
            return false
        }
        return true
    }

    /**
     * This method ends the communication with the server by releasing the
     * connection of the SocketAdapter and reset of the login and transaction state
     * of the ManagementHandler
     *
     * @return True after communication was successfully ended and connection resources released
     */
    override fun endCommunication(): Boolean {
        mSocket.releaseConnection()

        // reset the internal state
        resetTransactionState()
        resetLoginState()
        this.loginRequested = false
        return true
    }

    /**
     * The following methods emit the respective event to the server or rather forward
     * the associated event string and wrapped data to the SocketAdapter for transmission to the server
     */

    /*
    Login gibt Boolean zurück falls LogIn erfolgreich, außerdem hier mit Emit des LogIn Events
    gewartet, bis Server uns LOGIN_REQUEST gesendet hat (-> Zustandsvariable loginRequested)
    und Boolsche Rückgabe wird erst nach entspr. Event des Servers geschickt
     */
    override fun login(username: String, password: String): Boolean {

        var curWaitTimeForLogReq = 0L
        while (!this.loginRequested and (curWaitTimeForLogReq < MAX_TIMEWAIT_FOR_RESPONSE)) {
            Log.i(
                "SocketMHandler",
                "Wait with loginAttempt till loginRequest event (since $curWaitTimeForLogReq millis)"
            )
            curWaitTimeForLogReq += TIME_STEP_FOR_RESPONSE_WAIT
            Thread.sleep(TIME_STEP_FOR_RESPONSE_WAIT)

        }
        if (!loginRequested) {
            pushError(ManagementServerErrors.SERVER_DID_NOT_RESPOND)
            endCommunication()
            return false
        }

        val event: Event = Event.MANAGEMENT_LOGIN
        val data: JSONObjectWrapper = JSONLoginWrapper(username, password)
        mSocket.emitEventToServer(event.getEventString(), data)
        Log.i("SocketMHandler", "Login attempted")

        var curWaitTimeForResponse = 0L
        while (!this.loggedIn and !this.loginDenied and (curWaitTimeForResponse < MAX_TIMEWAIT_FOR_RESPONSE)) {
            curWaitTimeForResponse += TIME_STEP_FOR_RESPONSE_WAIT
            Thread.sleep(TIME_STEP_FOR_RESPONSE_WAIT)
        }
        if (!this.loggedIn and !this.loginDenied) {
            Log.i("SocketMHandler", "Server did not respond to login attempt since $curWaitTimeForResponse millis")
            pushError(ManagementServerErrors.SERVER_DID_NOT_RESPOND)
            endCommunication()
        }
        if (this.loggedIn) {
            Log.i("SocketMHandler", "Login was successful")
            return true
        } else if (this.loginDenied) {
            Log.i("SocketMHandler", "Login was denied")
            pushError(ManagementServerErrors.LOGIN_DENIED)
            endCommunication()
            initCommunication()
        }
        return false
    }

    /**
     * This method emits the "managementLogout@S" event to the server
     * and ends the communication afterwards
     *
     */
    override fun logout() {
        val event: Event = Event.MANAGEMENT_LOGOUT
        val data: JSONObjectWrapper = JSONEmptyWrapper()

        mSocket.emitEventToServer(event.getEventString(), data)
        endCommunication()
    }

    /**
     * This method emits the "resetPassword@S" event to the server,
     * with the associated username account as data to the server
     * (wrapped as JSONResetPasswordWrapper)
     *
     * @param username as String, specifies the account whose password should be reset
     */
    override fun resetPassword(username: String) {
        val event: Event = Event.RESET_PASSWORD
        val data: JSONObjectWrapper = JSONResetPasswordWrapper(username)

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    /**
     * This method emits the "changeManagementSettings@S" event to the server
     * with the given settings as data
     * (wrapped as JSONManagementSettingsWrapper)
     *
     * @param newPreferences as Preferences object, with the requested new settings
     */
    override fun changePreferences(newPreferences: Preferences) {
        val event: Event = Event.CHANGE_MANAGEMENT_SETTINGS
        val data: JSONObjectWrapper = JSONManagementSettingsWrapper(newPreferences)

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    /**
     * This method emits the "startTransaction@S" event to the server,
     * waiting for the response "transactionDenied@M or transactionStarted@M
     * to return
     *
     * @return True if the transaction could be started, else false
     */
    override fun startTransaction(): Boolean {

        val event: Event = Event.START_TRANSACTION
        val data: JSONObjectWrapper = JSONEmptyWrapper()
        mSocket.emitEventToServer(event.getEventString(), data)

        // wait for server response on requested transaction start
        var curWaitTimeForResp = 0L
        while (!transactionDenied and !transactionStarted and (curWaitTimeForResp < MAX_TIMEWAIT_FOR_RESPONSE)) {
            curWaitTimeForResp += TIME_STEP_FOR_RESPONSE_WAIT
            Thread.sleep(TIME_STEP_FOR_RESPONSE_WAIT)
        }

        when {
            transactionStarted -> {
                Log.i("SocketMHandler", "Transaction was started")
                return true
            }
            transactionDenied -> {
                Log.i("SocketMHandler", "Transaction was denied")
                resetTransactionState()
                pushError(ManagementServerErrors.TRANSACTION_DENIED)
                return false
            }
            else -> {
                Log.i("SocketMHandler", "No response for startTrans@S since $curWaitTimeForResp")
                pushError(ManagementServerErrors.SERVER_DID_NOT_RESPOND)
                endCommunication()
            }
        }
        return false
    }

    /**
     * This method emits the "abortTransaction@S" event to the server
     */
    override fun abortTransaction() {
        val event: Event = Event.ABORT_TRANSACTION
        val data: JSONObjectWrapper = JSONEmptyWrapper()
        mSocket.emitEventToServer(event.getEventString(), data)

        resetTransactionState()
    }

    /**
     * This method emits the "saveTransaction@S" event to the server
     */
    override fun saveTransaction() {
        val event: Event = Event.SAVE_TRANSACTION
        val data: JSONObjectWrapper = JSONEmptyWrapper()
        mSocket.emitEventToServer(event.getEventString(), data)

        resetTransactionState()
    }

    /**
     * This method emits the "addSpontaneousSlot@S" event to the server
     * with the given duration and timestamp of creation as data
     * (wrapped as JSONAddSpontaneousSlotWrapper)
     *
     * @param duration the requested duration of the new slot
     * @param timeOfCreation the timestamp of creation of the new slot
     */
    override fun addSpontaneousSlot(duration: Duration, timeOfCreation: DateTime) {
        val event: Event = Event.ADD_SPONTANEOUS_SLOT
        val data: JSONObjectWrapper = JSONAddSpontaneousSlotWrapper(duration, timeOfCreation)

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    /**
     * This method emits the "addFixedSlot@S" event to the server
     * with the given duration and appointment time as data
     * (wrapped as JSONAddFixedSlotWrapper)
     *
     * @param appointmentTime as Duration object, the requested duration for the new slot
     * @param duration as DateTime object, the requested appointment time for the new slot
     */
    override fun addFixedSlot(duration: Duration, appointmentTime: DateTime) {
        val event: Event = Event.ADD_FIXED_SLOT
        val data: JSONObjectWrapper = JSONAddFixedSlotWrapper(duration, appointmentTime)

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    /**
     * This method emits the "deleteSlot@S" event to the server
     * with the given slot code as data
     *
     * @param slotCode as String, specifies the slot to be deleted
     */
    override fun deleteSlot(slotCode: String) {
        val event: Event = Event.DELETE_SLOT
        val data: JSONObjectWrapper = JSONSlotCodeWrapper(slotCode)

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    /**
    * This method emits the "endCurrentSlot@S" event to the server
    *
    */
    override fun endCurrentSlot() {
        val event: Event = Event.END_CURRENT_SLOT
        val data: JSONObjectWrapper = JSONEmptyWrapper()

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    /**
     * This method emits the "moveSlotAfterAnother@S" event to the server,
     * with the both slot codes, whose order is to be changed, as data
     * (wrapped in JSONMoveSlotWrapper)
     *
     * @param movedSlot as String, specifies the slot that is to be moved
     * @param otherSlot as String, specifies the slot, after which the moved slot is to be placed
     */
    override fun moveSlotAfterAnother(movedSlot: String, otherSlot: String) {
        val event: Event = Event.MOVE_SLOT_AFTER_ANOTHER
        val data: JSONObjectWrapper = JSONMoveSlotWrapper(movedSlot, otherSlot)

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    /**
     * This method emits the "changeFixedSlotTime@S" event to the server,
     * with the slot code and new appointment time as data
     * (wrapped in JSONChangeSlotTimeWrapper)
     *
     * @param slotCode as String, specifies the slot whose appointment time is to be changed
     * @param newTime as DateTime object, specifies the request new appointment time
     */
    override fun changeFixedSlotTime(slotCode: String, newTime: DateTime) {
        val event: Event = Event.CHANGE_FIXED_SLOT_TIME
        val data: JSONObjectWrapper = JSONChangeSlotTimeWrapper(slotCode, newTime)

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    /**
     * This method emits the "changeSlotDuration@S" event to the server,
     * with the slot code and new duration as data
     * (wrapped in JSONChangeSlotDurationWrapper)
     *
     * @param slotCode as String, specifies the slot whose duration is to be changed
     * @param newDuration as Duration object, specifies the requested new duration
     */
    override fun changeSlotDuration(slotCode: String, newDuration: Duration) {
        val event: Event = Event.CHANGE_SLOT_DURATION
        val data: JSONObjectWrapper = JSONChangeSlotDurationWrapper(slotCode, newDuration)

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    /**
     * This method resets the login state of the ManagementHandler
     *
     */
    private fun  resetLoginState() {
        this.loggedIn = false
        this.loginDenied = false
    }

    /**
     * This method reset the transaction state of the ManagementHandler
     *
     */
    private fun resetTransactionState() {
        this.transactionDenied = false
        this.transactionStarted = false
    }

    /**
     * Getter method for the observable LiveData of the currently received
     * waiting queue
     *
     * @return current waiting queue of type ReceivedList (encapsulated by observable LiveData)
     */
    override fun getReceivedList(): LiveData<ReceivedList> {
        return this.currentList
    }

    /**
     * Getter method for the observable LiveData of the currently received
     * management settings
     *
     * @return current management settings of type Preferences (encapsulated by observable LiveData)
     */
    override fun getUpdatedPreferences(): LiveData<Preferences> {
        return this.currentPrefs
    }

    /**
     * The following callback methods are invoked by the SocketAdapter on the incoming of
     * the respective events from the server
     */

    /**
     * This callback method is invoked on the "transactionStarted@M" event,
     * which sets the transaction state of the ManagementHandler appropriately
     *
     * @param wrappedJSONData of type JSONEmptyWrapper as no further data was received
     */
    private fun onTransactionStarted(wrappedJSONData: JSONEmptyWrapper) {
        this.transactionStarted = true
    }

    /**
     * This callback method is invoked on the "transactionDenied@M" event,
     * which sets the transaction state of the ManagementHandler appropriately
     *
     * @param wrappedJSONData of type JSONEmptyWrapper as no further data was received
     */
    private fun onTransactionDenied(wrappedJSONData: JSONEmptyWrapper) {
        this.transactionDenied = true
    }

    /**
     * This callback method is invoked on the "loginRequest@M" event, which
     * sets the login state of the ManagementHandler accordingly so a new
     * login try can be emitted
     *
     * @param wrappedJSONData of type JSONEmptyWrapper as no further data was received
     */
    private fun onLoginRequest(wrappedJSONData: JSONEmptyWrapper) {
        this.loginRequested = true
    }

    /**
     * This callback method is invoked on the "invalidRequest@M" event,
     * pushing the error to the institute repository
     *
     * @param wrappedJSONData JSONErrorMessageWrapper containing the transmitted
     * error message
     */
    private fun onInvalidRequest(wrappedJSONData: JSONErrorMessageWrapper) {
        val errorMessage = wrappedJSONData.getErrorMessage()
        pushError(ManagementServerErrors.INVALID_REQUEST)
    }

    /**
     * This callback method is invoked on the "internaServerError@M", which
     * notifies the institute repository about the occurrence of an internal
     * server error
     *
     * @param wrappedJSONData the error message as JSONErrorMessageWrapper
     */
    private fun onInternalServerError(wrappedJSONData: JSONErrorMessageWrapper) {
        val errorMessage = wrappedJSONData.getErrorMessage()
        pushError(ManagementServerErrors.INTERNAL_SERVER_ERROR)
    }

    /**
     * This callback method is invoked on the event "managementLoginSuccess@M" which
     * sets the login state of the ManagementHandler appropriately
     *
     * @param wrappedJSONData of type JSONEmptyWrapper as no other data was transmitted
     */
    private fun onLoginSuccess(wrappedJSONData: JSONEmptyWrapper) {
        this.loggedIn = true
    }

    /**
     * This callback method is invoked on the event "managementLoginDenied@M" which
     * sets the login state of the ManagementHandler appropriately
     *
     * @param wrappedJSONData of type JSONEmptyWrapper as no other data was transmitted
     */
    private fun onLoginDenied(wrappedJSONData: JSONEmptyWrapper) {
        this.loginDenied = true
    }

    /**
     * This callback method is invoked on the event "updateManagementSettings@M" which
     * updates the newPrefs LiveData so the institute repository gets notified
     *
     * @param wrappedJSONData the received new settings as JSONManagementSettingsWrapper
     */
    private fun onUpdateManagementSettings(wrappedJSONData: JSONManagementSettingsWrapper) {
        val newPrefs = wrappedJSONData.getPreferences()
        this._currentPrefs.postValue(newPrefs)
    }

    /**
     * This callback method is invoked on the event "updateQueue@M" which
     * updates the receivedList LiveData so the institute repository gets notified
     *
     * @param wrappedJSONData the received queue as JSONQueueWrapper
     */
    private fun onUpdateQueue(wrappedJSONData: JSONQueueWrapper) {
        val receivedList = wrappedJSONData.getQueue()
        this._currentList.postValue(receivedList)
    }

    /**
     * This callback method is invoked on a network error, when the current connection
     * session is irrevocably lost, so the institute repository gets notified
     *
     * @param wrappedJSONData JSONEmptyWrapper, as no data was transmitted
     */
    private fun onNetworkError(wrappedJSONData: JSONEmptyWrapper) {
        pushError(ManagementServerErrors.NETWORK_ERROR)
        endCommunication()
    }

    /**
     * This method pushed errors to the institute repository via LiveData
     * for useful information about possibly time-displaced error events
     *
     * @param error of type ManagementServerErrors, the error to be pushed
     */
    private fun pushError(error: ManagementServerErrors){
        if (_errors.value !== null){
            val newList = _errors.value!!.plus(error).toMutableList()
            this._errors.postValue(newList)
        }else{
            this._errors.postValue(listOf(error))
        }
        Log.i("SocketMHandler", "Error $error was pushed")
    }
}
