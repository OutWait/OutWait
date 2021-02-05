package elite.kit.outwait.remoteDataSource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.customDataTypes.ReceivedList
import org.joda.time.DateTime
import org.joda.time.Duration

interface ManagementHandler {

    // Methoden um Verbindung zum Server aufzubauen/abzubauen (analog zu ClientHandler)
    fun initCommunication(): Boolean

    fun endCommunication(): Boolean

    fun login(username: String, password: String): Boolean

    fun logout(): Boolean

    fun resetPassword(username: String)

    fun changePreferences(newPreferences: Preferences)


    fun startTransaction()

    fun abortTransaction()

    fun saveTransaction()


    fun newSpontaneousSlot(duration: Duration)

    fun newFixedSlot(appointmentTime: DateTime, duration: Duration)


    fun deleteSlot(slotCode: String)

    fun endCurrentSlot()


    fun moveSlotAfterAnother(movedSlot: String, otherSlot: String)

    fun changeSlotDuration(slotCode: String, duration: Duration)

    fun changeFixedSlotTime(slotCode: String, newAppointmentTime: DateTime)




    /*
    Methoden um LiveData zum Observen zurückzugeben
     */
    fun getReceivedList(): LiveData<ReceivedList>

    fun getUpdatedPreferences(): LiveData<Preferences>


    /*
   LiveData um Events zu signalisieren? Eingeloggt, Ausgeloggt usw.
   //TODO Was wird hier gebraucht? Am besten mit LiveData?
   //Wie im AndroidKurs muss man dann auch sicherstellen, dass onEventFinish() aufgerufen wird!!
    */

}
