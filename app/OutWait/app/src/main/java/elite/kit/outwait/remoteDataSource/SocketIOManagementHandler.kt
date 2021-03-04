package elite.kit.outwait.remoteDataSource

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.customDataTypes.ReceivedList
import elite.kit.outwait.networkProtocol.*
import org.joda.time.DateTime
import org.joda.time.Duration

// TODO Falschen Zugriff durchs Repo verhindern (bspw. Änderung ohne gestartete Transaction)
// TODO InvalidRequest adäquat handlen (an Repo weiter?) Was für Fehler sind möglich?
// TODO Interne Zustandsvariablen richtig benutzt? Threading beachtet?
// insb. login- und transaction- related sachen?

private const val MAX_WAITTIME_FOR_RESPONSE = 10000L
private const val TIME_STEP_FOR_RESPONSE_WAIT = 1000L


class SocketIOManagementHandler : ManagementHandler {



    // Ein Haufen unschöner (intern, da bisher prozedural) Zustandsvariablen
    // TODO LiveData schöner?
    private var loginRequested = false
    private var loggedIn = false
    private var loginDenied = false
    private var transactionStarted = false
    private var transactionDenied = false

    private val _errors = MutableLiveData<List<ManagementServerErrors>>()
    override fun getErrors() = _errors as LiveData<List<ManagementServerErrors>>

    /*
    External and internal LiveData (encapsulated with backing property) for current WaitingQueue
    */
    private val _currentList = MutableLiveData<ReceivedList>(null)
    private val currentList: LiveData<ReceivedList>
        get() = _currentList


    /*
    External and internal LiveData (encapsulated with backing property) for current Preferences
     */
    private val _currentPrefs = MutableLiveData<Preferences>(null)
    private val currentPrefs: LiveData<Preferences>
        get() = _currentPrefs

    private val namespaceManagement: String = "/management"

    private val managementEventToCallbackMapping: HashMap<Event,
            (wrappedJSONData: JSONObjectWrapper) -> Unit> = hashMapOf()

    private val mSocket: SocketAdapter

    //private var currentSessionID: String = ""

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
    }

    //TODO initComm was noch zu tun?
    override fun initCommunication(): Boolean {
        if (!mSocket.initializeConnection(managementEventToCallbackMapping)) {
            pushError(ManagementServerErrors.COULD_NOT_CONNECT)
            endCommunication()
            return false
        } else {
            //TODO Wenn man das rauslöscht funktionierts plötzlich?
           /* this.currentSessionID = mSocket.getCurrentSessionID()
            Log.i("SocketMHandler", "Connection established with $currentSessionID id")

            */
        }
        return true
    }

    override fun endCommunication(): Boolean {
        mSocket.releaseConnection()

        // interne Zustandsvariablen zurücksetzen
        resetTransactionState()
        resetLoginState()
        this.loginRequested = false
        return true
    }


    /*
    Login gibt Boolean zurück falls LogIn erfolgreich, außerdem hier mit Emit des LogIn Events
    gewartet, bis Server uns LOGIN_REQUEST gesendet hat (-> Zustandsvariable loginRequested)
    und Boolsche Rückgabe wird erst nach entspr. Event des Servers geschickt
     */
    override fun login(username: String, password: String): Boolean {

        var curWaitTimeForLogReq = 0L
        while (!this.loginRequested and (curWaitTimeForLogReq < MAX_WAITTIME_FOR_RESPONSE)) {
            Log.i(
                "SocketIOManagHandl",
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
        Log.i("SocketIOManagementHandl", "Login attempted")

        var curWaitTimeForResponse = 0L
        while (!this.loggedIn and !this.loginDenied and (curWaitTimeForResponse < MAX_WAITTIME_FOR_RESPONSE)) {
            Log.i("SocketIOManagHandl", "Waiting on server for LoginResponse (since $curWaitTimeForResponse millis)")
            curWaitTimeForResponse += TIME_STEP_FOR_RESPONSE_WAIT
            Thread.sleep(TIME_STEP_FOR_RESPONSE_WAIT)
        }
        if (!this.loggedIn and !this.loginDenied) {
            Log.i("SocketMHandler", "Server did not respond to login attempt since $curWaitTimeForResponse millis")
            pushError(ManagementServerErrors.SERVER_DID_NOT_RESPOND)
            endCommunication()
        }
        if (this.loggedIn) {
            Log.i(
                "SocketIOManagementHandl",
                "Login was successful"
            )
            return true
        } else if (this.loginDenied) {
            Log.i("SocketIOManagementHandl", "Login was denied")
            pushError(ManagementServerErrors.LOGIN_DENIED)
            endCommunication()
            Log.i("SocketMHandler", "We ended Comm")
            //TODO >>>>>>>>>>>>>>< Hier gehts kaputt >>>>>>>>>>>>>>>
            initCommunication()
            Log.i("SocketMHandler", "We initComm again")
            // <<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>
        }
        return false
    }

    override fun logout() {
        val event: Event = Event.MANAGEMENT_LOGOUT
        val data: JSONObjectWrapper = JSONEmptyWrapper()

        mSocket.emitEventToServer(event.getEventString(), data)
        this.loggedIn = false
    }

    override fun resetPassword(username: String) {
        val event: Event = Event.RESET_PASSWORD
        val data: JSONObjectWrapper = JSONResetPasswordWrapper(username)

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    override fun changePreferences(newPreferences: Preferences) {
        val event: Event = Event.CHANGE_MANAGEMENT_SETTINGS
        val data: JSONObjectWrapper = JSONManagementSettingsWrapper(newPreferences)

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    /*
   startTransaction gibt Boolean zurück falls Start erfolgreich, boolsche Rückgabe wird erst
   gemacht nachdem Server auf den StartVersuch geantwortet hat (mit TransactionSuccess oder Denied)
     */
    override fun startTransaction(): Boolean {

        val event: Event = Event.START_TRANSACTION
        val data: JSONObjectWrapper = JSONEmptyWrapper()
        mSocket.emitEventToServer(event.getEventString(), data)

        // warte in while loop auf Antwort vom Server
        var curWaitTimeForResp = 0L
        while (!transactionDenied and !transactionStarted and (curWaitTimeForResp < MAX_WAITTIME_FOR_RESPONSE)) {
            Log.i("SocketIOManagementHandl",
                "Waiting on server response for transactionStart since $curWaitTimeForResp millis")
            curWaitTimeForResp += TIME_STEP_FOR_RESPONSE_WAIT
            Thread.sleep(TIME_STEP_FOR_RESPONSE_WAIT)
        }

        when {
            transactionStarted -> {
                Log.i("SocketIOManagementHandl", "Transaction was started")
                return true
            }
            transactionDenied -> {
                Log.i("SocketIOManagementHandl", "Transaction was denied")
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

    override fun abortTransaction() {
        val event: Event = Event.ABORT_TRANSACTION
        val data: JSONObjectWrapper = JSONEmptyWrapper()
        mSocket.emitEventToServer(event.getEventString(), data)

        resetTransactionState()
    }

    override fun saveTransaction() {
        val event: Event = Event.SAVE_TRANSACTION
        val data: JSONObjectWrapper = JSONEmptyWrapper()
        mSocket.emitEventToServer(event.getEventString(), data)

        resetTransactionState()
    }

    override fun addSpontaneousSlot(duration: Duration, timeOfCreation: DateTime) {
        val event: Event = Event.ADD_SPONTANEOUS_SLOT
        val data: JSONObjectWrapper = JSONAddSpontaneousSlotWrapper(duration, timeOfCreation)

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    override fun addFixedSlot(duration: Duration, appointmentTime: DateTime) {
        val event: Event = Event.ADD_FIXED_SLOT
        val data: JSONObjectWrapper = JSONAddFixedSlotWrapper(duration, appointmentTime)

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    override fun deleteSlot(slotCode: String) {
        val event: Event = Event.DELETE_SLOT
        val data: JSONObjectWrapper = JSONSlotCodeWrapper(slotCode)

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    override fun endCurrentSlot() {
        val event: Event = Event.END_CURRENT_SLOT
        val data: JSONObjectWrapper = JSONEmptyWrapper()

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    override fun moveSlotAfterAnother(movedSlot: String, otherSlot: String) {
        val event: Event = Event.MOVE_SLOT_AFTER_ANOTHER
        val data: JSONObjectWrapper = JSONMoveSlotWrapper(movedSlot, otherSlot)

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    override fun changeFixedSlotTime(slotCode: String, newTime: DateTime) {
        val event: Event = Event.CHANGE_FIXED_SLOT_TIME
        val data: JSONObjectWrapper = JSONChangeSlotTimeWrapper(slotCode, newTime)

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    override fun changeSlotDuration(slotCode: String, newDuration: Duration) {
        val event: Event = Event.CHANGE_SLOT_DURATION
        val data: JSONObjectWrapper = JSONChangeSlotDurationWrapper(slotCode, newDuration)

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    private fun  resetLoginState() {
        this.loggedIn = false
        this.loginDenied = false
    }

    private fun resetTransactionState() {
        this.transactionDenied = false
        this.transactionStarted = false
    }

    /*
    Getter für die LiveData Attribute
     */
    override fun getReceivedList(): LiveData<ReceivedList> {
        return this.currentList
    }

    override fun getUpdatedPreferences(): LiveData<Preferences> {
        return this.currentPrefs
    }

    /*
    Die Callback Methoden die gemäß Mapping bei einem eingeheneden Event aufgerufen werden
     */

    private fun onTransactionStarted(wrappedJSONData: JSONEmptyWrapper) {
        this.transactionStarted = true
    }

    private fun onTransactionDenied(wrappedJSONData: JSONEmptyWrapper) {
        this.transactionDenied = true
    }

    private fun onLoginRequest(wrappedJSONData: JSONEmptyWrapper) {
        this.loginRequested = true
    }

    //TODO Error Message String hochreichen ans Repo
    private fun onInvalidRequest(wrappedJSONData: JSONErrorMessageWrapper) {
        val errorMessage = wrappedJSONData.getErrorMessage()
        pushError(ManagementServerErrors.INVALID_REQUEST)
    }

    private fun onInternalServerError(wrappedJSONData: JSONErrorMessageWrapper) {
        val errorMessage = wrappedJSONData.getErrorMessage()
        pushError(ManagementServerErrors.INTERNAL_SERVER_ERROR)
    }

    private fun onLoginSuccess(wrappedJSONData: JSONEmptyWrapper) {
        this.loggedIn = true
    }

    private fun onLoginDenied(wrappedJSONData: JSONEmptyWrapper) {
        this.loginDenied = true
    }


    private fun onUpdateManagementSettings(wrappedJSONData: JSONManagementSettingsWrapper) {
        val newPrefs = wrappedJSONData.getPreferences()
        this._currentPrefs.postValue(newPrefs)
    }

    private fun onUpdateQueue(wrappedJSONData: JSONQueueWrapper) {
        val receivedList = wrappedJSONData.getQueue()
        this._currentList.postValue(receivedList)
    }

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
