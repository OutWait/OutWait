package edu.kit.outwait.instituteRepository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import edu.kit.outwait.customDataTypes.Mode
import edu.kit.outwait.customDataTypes.Preferences
import edu.kit.outwait.customDataTypes.ReceivedList
import edu.kit.outwait.customDataTypes.ReceivedListUtil
import edu.kit.outwait.remoteDataSource.ManagementHandler
import edu.kit.outwait.remoteDataSource.ManagementServerErrors
import org.joda.time.DateTime
import org.joda.time.Duration

/**
 * Simulates rudimentarily the Managemant handler of the remote data source
 *
 */
class ManagementHandlerFake : ManagementHandler {

    companion object {
        /**
         * with this username and password you can simulate a successful login
         */
        const val VALID_USERNAME = "validU"
        /**
         * with this username and password you can simulate a successful login
         */
        const val VALID_PASSWORD = "validP"
        /**
         * with this username and password you can simulate an invalid login
         */
        const val INVALID_USERNAME = "invalidU"
        /**
         * with this username and password you can simulate an invalid login
         */
        const val INVALID_PASSWORD = "validP"
        /**
         * After login the server will send a queue with this amount of
         * spontaneous slots
         */
        const val NR_SLOTS_AFTER_LOGIN = 3
    }

    /**
     * change it to simulate a loss of network connection or a stable connection
     */
    var internetWorks = true

    private var connected = false

    private var inTransaction = false

    private val receivedList = MutableLiveData<ReceivedList>()

    private val preferences = MutableLiveData<Preferences>()

    private val errorNotifications = MutableLiveData<List<ManagementServerErrors>>(listOf())

    override fun initCommunication(): Boolean {
        return if (internetWorks && !connected){
            connected = true
            true
        } else {
            false
        }

    }

    override fun endCommunication(): Boolean {
        return if (internetWorks && connected){
            connected = false
            true
        } else false
    }

    override fun login(username: String, password: String): Boolean {
        if (connected
            && internetWorks
            && username == VALID_USERNAME
            && password == VALID_PASSWORD) {
            receivedList.value = ReceivedListUtil.prepareReceivedList(NR_SLOTS_AFTER_LOGIN)

            preferences.value = Preferences(
                Duration.standardMinutes(30),
                Duration.standardMinutes(30),
                Duration.standardMinutes(30),
                Duration.standardMinutes(30),
                Mode.ONE
            )
            return true
        } else if (connected
            && internetWorks
            && username == INVALID_USERNAME
            && password == INVALID_PASSWORD) {
            pushError(ManagementServerErrors.LOGIN_DENIED)
        }
        return false
    }

    override fun logout() {connected = false}

    override fun resetPassword(username: String) = throw NotImplementedError()

    override fun changePreferences(newPreferences: Preferences) {
        preferences.value = Preferences(
            Duration.standardMinutes(30),
            Duration.standardMinutes(30),
            Duration.standardMinutes(30),
            Duration.standardMinutes(30),
            Mode.ONE
        )
    }

    override fun startTransaction(): Boolean {
        if (internetWorks) {
            inTransaction = true
            return true
        }
        return false
    }

    override fun abortTransaction() {
        newReceivedList()
        inTransaction = false
    }

    override fun saveTransaction() {
        if (internetWorks) {
            inTransaction = false
        }
    }

    /*
     * All queue manipulation methods do not really change the queue but only set a new queue
     * so that we can check if the queue has refreshed. We dont want to fake the complex server
     * queue manipulations
     */
    override fun addSpontaneousSlot(duration: Duration, timeOfCreation: DateTime) = newReceivedList()
    override fun addFixedSlot(duration: Duration, appointmentTime: DateTime) = newReceivedList()
    override fun deleteSlot(slotCode: String) = newReceivedList()
    override fun endCurrentSlot() = newReceivedList()
    override fun moveSlotAfterAnother(movedSlot: String, otherSlot: String) = newReceivedList()
    override fun changeSlotDuration(slotCode: String, newDuration: Duration) = newReceivedList()
    override fun changeFixedSlotTime(slotCode: String, newTime: DateTime) = newReceivedList()


    override fun getReceivedList() = receivedList as LiveData<ReceivedList>

    override fun getUpdatedPreferences() = preferences as LiveData<Preferences>

    override fun getErrors() = errorNotifications as LiveData<List<ManagementServerErrors>>

    private fun newReceivedList(){
        receivedList.value = ReceivedListUtil.prepareReceivedList(3)
    }

    private fun pushError(error: ManagementServerErrors) {
        if (errorNotifications.value !== null) {
            val newList = errorNotifications.value!!.plus(error).toMutableList()
            errorNotifications.value = newList
        } else {
            errorNotifications.value = listOf(error)
        }
    }
}
