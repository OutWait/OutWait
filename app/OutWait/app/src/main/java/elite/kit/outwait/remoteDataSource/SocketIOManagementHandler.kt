package elite.kit.outwait.remoteDataSource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.customDataTypes.ReceivedList
import io.socket.client.Socket
import org.joda.time.DateTime
import org.joda.time.Duration
import org.json.JSONObject

class SocketIOManagementHandler : ManagementHandler {

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
        TODO("Not yet implemented")
    }

    override fun logout(): Boolean {
        TODO("Not yet implemented")
    }

    override fun resetPassword(username: String) {
        TODO("Not yet implemented")
    }

    override fun changePreferences(newPreferences: Preferences) {
        TODO("Not yet implemented")
    }

    override fun startTransaction() {
        TODO("Not yet implemented")
    }

    override fun abortTransaction() {
        TODO("Not yet implemented")
    }

    override fun saveTransaction() {
        TODO("Not yet implemented")
    }

    override fun newSpontaneousSlot(duration: Duration) {
        TODO("Not yet implemented")
    }

    override fun newFixedSlot(appointmentTime: DateTime, duration: Duration) {
        TODO("Not yet implemented")
    }

    override fun deleteSlot(slotCode: String) {
        TODO("Not yet implemented")
    }

    override fun endCurrentSlot() {
        TODO("Not yet implemented")
    }

    override fun moveSlotAfterAnother(movedSlot: String, otherSlot: String) {
        TODO("Not yet implemented")
    }

    override fun changeFixedSlotTime(slotCode: String, newAppointmentTime: DateTime) {
        TODO("Not yet implemented")
    }

    override fun changeSlotDuration(slotCode: String, duration: Duration) {
        TODO("Not yet implemented")
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
