package elite.kit.outwait.remoteDataSource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.customDataTypes.ReceivedList
import elite.kit.outwait.networkProtocol.*
import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

class SocketIOManagementHandler : ManagementHandler {

    /* TODO Keine LiveData machen, da intern und Repo soll nix von Komm Impl wissen
    Variable die anzeigt, ob der Server einen Login zulässt (Event.LOGIN_REQUEST wurde
    empfangen und verarbeitet) vorherige Login-Anfragen durch das Repo
        a) werfen Fehlermeldung o.ä   oder    b) lassen den Thread warten?
     */
    private var loginRequested = false
    /*
    TODO Variable die anzeigt, ob Manager gerade eingeloggt ist oder nicht -> LiveData machen??
     */
    private var managerLoggedIn = false

    private val namespaceManagement: String = "/management"

    private val mSocket: SocketAdapter

    init {
        mSocket = SocketAdapter(namespaceManagement)
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

     */

    private val currentList = MutableLiveData<ReceivedList>()
    private val _currentList : LiveData<ReceivedList>
        get() = currentList


    /*
    Live Data um die aktuellen ManagementEinstellungen (Preferences)
    lesbar zu machen, einmal Mutable für intern und plain
    LiveData für READ_ONLY
     */

    private val currentPrefs = MutableLiveData<Preferences>()
    private val _currentPrefs : LiveData<Preferences>
        get() = currentPrefs


    //TODO Absprache mit Benni wie hier zurückgegeben /gewartet wird
    override fun login(username: String, password: String): Boolean {
        // TODO warte bis wir einen Login-Request vom Server verarbeitet haben
        while (loginRequested == false) {
            Thread.sleep(1_000)
        }

        val event: Event = Event.MANAGEMENT_LOGIN
        val data: JSONObjectWrapper = JSONLoginWrapper(username, password)

        mSocket.emitEventToServer(event.getEventString(), data)

        // TODO wirklich nötig hier einen Boolean zurückzugeben? Lieber
        // Zustand LoggedIn mit LiveData dem Repo mitteilen? oder wieder warten bis
        // Login vom Server zurückkommt? (mittels Sleep und interner Zustandsvar?)?
        return true
    }

    //TODO Absprache mit Benni ob hier was zurückggeben werden muss
    override fun logout(): Boolean {
        val event: Event = Event.MANAGEMENT_LOGOUT
        val data: JSONObjectWrapper = JSONEmptyWrapper()

        mSocket.emitEventToServer(event.getEventString(), data)

        // TODO wirklich nötig hier einen Boolean zurückzugeben? Lieber
        // Zustand LoggedIn mit LiveData dem Repo mitteilen?
        return true
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

    //TODO Absprache mit Benni wie hier zurückgegeben /gewartet wird
    override fun startTransaction() {
        val event: Event = Event.START_TRANSACTION
        val data: JSONObjectWrapper = JSONEmptyWrapper()

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    override fun abortTransaction() {
        val event: Event = Event.ABORT_TRANSACTION
        val data: JSONObjectWrapper = JSONEmptyWrapper()

        mSocket.emitEventToServer(event.getEventString(), data)
    }

    //TODO Absprache mit Benni wie hier zurückgegeben /gewartet wird
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

    override fun getUpdatedPreferences() : LiveData<Preferences> {
        return _currentPrefs
    }

    private fun processIncomingEvent(event: Event, wrappedJSONData: JSONObjectWrapper) {

        //TODO Strategie verwenden um Daten zu verarbeiten

    }

}
