package elite.kit.outwait.remoteDataSource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.customDataTypes.ReceivedList
import elite.kit.outwait.networkProtocol.ManagementEvents
import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

const val namespaceManagement: String = "/management"

class SocketIOManagementHandler : ManagementHandler {

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


    override fun login(username: String, password: String): Boolean {
        var data:JSONObject = JSONObject()
        data.put("username", username)
        data.put("password", username)
        //TODO Wie failsafe emitten? Was abfangen? Was returnen?
        return true
    }

    override fun logout(): Boolean {
        mSocket.emitEventToServer(ManagementEvents.MANAGEMENT_LOGOUT.getEventString())
        //TODO Rückgabe als Boolean nötig? Ggf falls sonst Fehlermeldung geworfen?
        return true
    }

    override fun resetPassword(username: String) {
        var data:JSONObject = JSONObject()
        data.put("username", username)
        mSocket.emitEventToServer(ManagementEvents.RESET_PASSWORD.getEventString(), data)
    }

    override fun changePreferences(newPreferences: Preferences) {
        TODO("Not yet implemented")
        //TODO JSONObjWrapper für Preferences
    }

    override fun startTransaction() {
        mSocket.emitEventToServer(ManagementEvents.START_TRANSACTION.getEventString())
    }

    override fun abortTransaction() {
        mSocket.emitEventToServer((ManagementEvents.ABORT_TRANSACTION.getEventString()))
    }

    override fun saveTransaction() {
        mSocket.emitEventToServer((ManagementEvents.SAVE_TRANSACTION.getEventString()))
    }

    override fun addSpontaneousSlot(duration: Duration, timeOfCreation: DateTime) {
        TODO("Not yet implemented")
        //TODO Wrapper? Joda Time Einheiten in UNIX timestamp umwandeln
    }

    override fun addFixedSlot(duration: Duration, appointmentTime: DateTime) {
        TODO("Not yet implemented")
        //TODO Wrapper? Joda Time Einheiten in UNIX timestamp umwandeln
    }

    override fun deleteSlot(slotCode: String) {
        var data: JSONObject = JSONObject()
        data.put("slotCode", slotCode)
        mSocket.emitEventToServer(ManagementEvents.DELETE_SLOT.getEventString(), data)
    }

    override fun endCurrentSlot() {
        mSocket.emitEventToServer((ManagementEvents.END_CURRENT_SLOT.getEventString()))
    }

    override fun moveSlotAfterAnother(movedSlot: String, otherSlot: String) {
        var data: JSONObject = JSONObject()
        data.put("movedSlot", movedSlot)
        data.put("otherSlot", otherSlot)
        mSocket.emitEventToServer(ManagementEvents.MOVE_SLOT_AFTER_ANOTHER.getEventString(), data)
    }

    override fun changeFixedSlotTime(slotCode: String, newTime: DateTime) {
        TODO("Not yet implemented")
        //TODO Wrapper? Joda Time Einheiten in UNIX timestamp umwandeln
    }

    override fun changeSlotDuration(slotCode: String, newDuration: Duration) {
        TODO("Not yet implemented")
        //TODO Wrapper? Joda Time Einheiten in UNIX timestamp umwandeln
    }

    override fun getReceivedList(): LiveData<ReceivedList> {
        return _currentList
    }

    override fun getUpdatedPreferences() : LiveData<Preferences> {
        return _currentPrefs
    }

    private fun processIncomingEvent(event: String, data: JSONObject) {

        //TODO Strategie verwenden um Event und seine Daten zu verarbeiten
    }






}
