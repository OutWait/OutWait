package elite.kit.outwait.remoteDataSource

import androidx.lifecycle.LiveData
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.customDataTypes.ReceivedList
import org.joda.time.DateTime
import org.joda.time.Duration

interface ManagementHandler {

    // Methoden um Verbindung zum Server aufzubauen/abzubauen (analog zu ClientHandler)
    fun initCommunication(): Boolean

    fun endCommunication(): Boolean

    fun login(username: String, password: String): Boolean

    fun logout()

    fun resetPassword(username: String)

    fun changePreferences(newPreferences: Preferences)


    fun startTransaction(): Boolean

    fun abortTransaction()

    fun saveTransaction()


    fun addSpontaneousSlot(duration: Duration, timeOfCreation: DateTime)

    fun addFixedSlot(duration: Duration, appointmentTime: DateTime)


    fun deleteSlot(slotCode: String)

    fun endCurrentSlot()


    fun moveSlotAfterAnother(movedSlot: String, otherSlot: String)

    fun changeSlotDuration(slotCode: String, newDuration: Duration)

    fun changeFixedSlotTime(slotCode: String, newTime: DateTime)




    /*
    Methoden um LiveData zum Observen zurückzugeben
     */
    fun getReceivedList(): LiveData<ReceivedList>

    fun getUpdatedPreferences(): LiveData<Preferences>

    // TODO Welche LiveData bräuchte man noch (Bspw. bei Callbacks für Events vom Server)

}
