package elite.kit.outwait.remoteDataSource

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.customDataTypes.ReceivedList
import elite.kit.outwait.networkProtocol.*
import org.joda.time.DateTime
import org.joda.time.Duration


class SocketIOManagementHandler : ManagementHandler {

    /* TODO Keine LiveData machen, da intern und Repo soll nix von Komm Impl wissen
    Variable die anzeigt, ob der Server einen Login zulässt (Event.LOGIN_REQUEST wurde
    empfangen und verarbeitet) vorherige Login-Anfragen durch das Repo
        a) werfen Fehlermeldung o.ä   oder    b) lassen den Thread warten?
     */
    private var loginRequested = false

    private var loggedIn = false
    private var loginDenied = false
    private var transactionStarted = false
    private var transactionDenied = false

    /*
    TODO Variable die anzeigt, ob Manager gerade eingeloggt ist oder nicht -> LiveData machen??
     */
    private var managerLoggedIn = false

    private val namespaceManagement: String = "/management"

    private val managementEventToCallbackMapping: HashMap<Event,
            (wrappedJSONData: JSONObjectWrapper) -> Unit> = hashMapOf()

    private val mSocket: SocketAdapter

    init {
        mSocket = SocketAdapter(namespaceManagement)

        // configure HashMap that maps receiving events to callbacks
        managementEventToCallbackMapping[Event.TRANSACTION_STARTED] = { receivedData ->
            onTransactionStarted(receivedData as JSONEmptyWrapper)
        }
        managementEventToCallbackMapping[Event.TRANSACTION_DENIED] = { receivedData ->
            onTransactionDenied(receivedData as JSONEmptyWrapper)
        }
        managementEventToCallbackMapping[Event.LOGIN_REQUEST] = { receivedData ->
            onLoginRequest(receivedData as JSONEmptyWrapper)
        }
        managementEventToCallbackMapping[Event.INVALID_REQUEST] = { receivedData ->
            onInvalidRequest(receivedData as JSONInvalidRequestWrapper)
        }
        managementEventToCallbackMapping[Event.MANAGEMENT_LOGIN_SUCCESS] = { receivedData ->
            onLoginSuccess(receivedData as JSONEmptyWrapper)
        }
        managementEventToCallbackMapping[Event.MANAGEMENT_LOGIN_DENIED] = { receivedData ->
            onLoginDenied(receivedData as JSONEmptyWrapper)
        }
        managementEventToCallbackMapping[Event.UPDATE_MANAGEMENT_SETTINGS] = { receivedData ->
            onUpdateManagementSettings(receivedData as JSONManagementSettingsWrapper)
        }
        managementEventToCallbackMapping[Event.UPDATE_QUEUE] = { receivedData ->
            onUpdateQueue(receivedData as JSONQueueWrapper)
        }
    }

    //TODO Mit ObjectWrappern die Daten zum versenden verpacken
    //TODO Mit Strategie (oder internen Methoden, da net so viele) die incomingEvents verarbeiten
    //Falls nur interne Methoden, dann diese direkt in Event-Callback-Mapping einfügen?

    override fun initCommunication(): Boolean {
        TODO("Not yet implemented")
    }

    override fun endCommunication(): Boolean {
        TODO("Not yet implemented")
    }

    /*
    Live Data um die aktuelle WaitingQueue lesbar zu machen, einmal Mutable für intern und
    plain LiveData für READ_ONLY
    TODO Die LiveData bzw. die darunter liegenden Werte zu Beginn mit Null init, damit die BEnni schon getten kann
     */

    private val currentList = MutableLiveData<ReceivedList>()
    private val _currentList: LiveData<ReceivedList>
        get() = currentList


    /*
    Live Data um die aktuellen ManagementEinstellungen (Preferences)
    lesbar zu machen, einmal Mutable für intern und plain
    LiveData für READ_ONLY
    TODO Die LiveData bzw. die darunter liegenden Werte zu Beginn mit Null init, damit die BEnni schon getten kann
     */

    private val currentPrefs = MutableLiveData<Preferences>()
    private val _currentPrefs: LiveData<Preferences>
        get() = currentPrefs


    /*
    Login gibt Boolean zurück falls LogIn erfolgreich, außerdem hier mit Emit des LogIn Events
    gewartet, bis Server uns LOGIN_REQUEST gesendet hat (-> Zustandsvariable loginRequested)
    und Boolsche Rückgabe wird erst nach entspr. Event des Servers geschickt
     */
    override fun login(username: String, password: String): Boolean {
        // TODO warte bis wir einen Login-Request vom Server verarbeitet haben
        while (!this.loginRequested) {
            Log.i(
                "SocketIOManagementHandl",
                "Waiting on server LoginRequest for LoginAttempt"
            )
            Thread.sleep(1_000)
        }

        val event: Event = Event.MANAGEMENT_LOGIN
        val data: JSONObjectWrapper = JSONLoginWrapper(username, password)

        mSocket.emitEventToServer(event.getEventString(), data)

        Log.i(
            "SocketIOManagementHandl",
            "Login attempted"
        )

        while (!this.loggedIn and !this.loginDenied) {
            Log.i(
                "SocketIOManagementHandl",
                "Waiting on server for LoginResponse"
            )
            Thread.sleep(1_000)
        }

        if (this.loggedIn) {
            Log.i(
                "SocketIOManagementHandl",
                "Login was successful"
            )
            return true
        } else if (this.loginDenied) {
            Log.i(
                "SocketIOManagementHandl",
                "Login was denied"
            )
        }

        return false
    }

    override fun logout() {
        val event: Event = Event.MANAGEMENT_LOGOUT
        val data: JSONObjectWrapper = JSONEmptyWrapper()

        mSocket.emitEventToServer(event.getEventString(), data)

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

        while (!transactionDenied and !transactionStarted) {
            Log.i(
                "SocketIOManagementHandl",
                "Waiting on server response for transactionStart"
            )
            Thread.sleep(1_000)

        }

        if (transactionStarted) {
            Log.i("SocketIOManagementHandl", "Transaction was started")
            return true
        } else if (transactionDenied) {
            Log.i("SocketIOManagementHandl", "Transaction was denied")
        }

        return false
    }

    override fun abortTransaction() {
        val event: Event = Event.ABORT_TRANSACTION
        val data: JSONObjectWrapper = JSONEmptyWrapper()

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    override fun saveTransaction() {
        val event: Event = Event.SAVE_TRANSACTION
        val data: JSONObjectWrapper = JSONEmptyWrapper()

        mSocket.emitEventToServer(event.getEventString(), data)
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

    /*
    Getter für die LiveData Attribute TODO Welche LiveData fehlt noch?
     */
    override fun getReceivedList(): LiveData<ReceivedList> {
        return _currentList
    }

    override fun getUpdatedPreferences(): LiveData<Preferences> {
        return _currentPrefs
    }

    /*
    Die Callback Methoden die gemäß Mapping bei einem eingeheneden Event aufgerufen werden
     */

    private fun onTransactionStarted(wrappedJSONData: JSONEmptyWrapper) {
        this.transactionStarted = true
        // TODO Zustandsvariable wieder zurücksetzen? Wann?
    }

    private fun onTransactionDenied(wrappedJSONData: JSONEmptyWrapper) {
        this.transactionDenied = true
    }

    private fun onLoginRequest(wrappedJSONData: JSONEmptyWrapper) {
        this.loginRequested = true
    }

    private fun onInvalidRequest(wrappedJSONData: JSONInvalidRequestWrapper) {
        val errorMessage = wrappedJSONData.getErrorMessage()
        TODO("Fehlermeldung werfen")
        // TODO Was noch?
    }

    private fun onLoginSuccess(wrappedJSONData: JSONEmptyWrapper) {
        this.loggedIn = true
    }

    private fun onLoginDenied(wrappedJSONData: JSONEmptyWrapper) {
        this.loginDenied = true
        TODO("Server will hier Verbindung abbrechen!! Was tun?")
    }


    private fun onUpdateManagementSettings(wrappedJSONData: JSONManagementSettingsWrapper) {
        val newPrefs = wrappedJSONData.getPreferences()
        TODO("Preferences Object mit neuen Preferences in LiveData aktualisieren")

    }

    private fun onUpdateQueue(wrappedJSONData: JSONQueueWrapper) {
        val receivedList = wrappedJSONData.getQueue()
        TODO("ReceivedList Object mit neuer ReceivedList in LiveData aktualisieren")
    }


}
