package edu.kit.outwait.remoteDataSource

import androidx.lifecycle.LiveData
import edu.kit.outwait.customDataTypes.Preferences
import edu.kit.outwait.customDataTypes.ReceivedList
import org.joda.time.DateTime
import org.joda.time.Duration

/**
 * This interface "ManagementHandler" represents the "abstract product"
 * in the, here used and commonly known as, "abstract factory pattern".
 * It describes all methods for network communication, that the
 * institute repository (or higher tier) can use to send to and receive data from the server.
 * They must be provided by the "concrete product" of this pattern, meaning the
 * respective implementation of the network communication, implementing this interface
 *
 */
interface ManagementHandler {

    /**
     * This abstract method sets up the communication resources, depending on the used
     * implementation of client-server communication (e.g WebSockets, REST etc)
     *
     * @return true, if communication was successfully established, else false
     */
    fun initCommunication(): Boolean

    /**
     * This abstract method releases all communication resources, depending on the used
     * implementation of client-server communication (e.g WebSockets, REST etc)
     *
     * @return true, if communication was successfully released, else false
     */
    fun endCommunication(): Boolean

    /**
     * This abstract method sends the event "managementLogin@S" and the credentials as data
     * to the server, informing about a requested login
     *
     * @param username as String, specifies the user account to be logged in
     * @param password as String, specifies the associated password
     * @return If the login was successful returns true, else false
     */
    fun login(username: String, password: String): Boolean

    /**
     * This abstract method sends the event "managamentLogout@S" to the server,
     * informing about a performed logout
     *
     */
    fun logout()

    /**
     * This abstract method sends the event "resetPassword@S" with the given username
     * as data to the server
     *
     * @param username as String, specifies the user account for which the password is
     * to be reset
     */
    fun resetPassword(username: String)

    /**
     * This abstract method sends the event "changePreferences@S" with the new management settings
     * as data to the server
     *
     * @param newPreferences the new (requested) management settings as Preferences object
     */
    fun changePreferences(newPreferences: Preferences)

    /**
     * This abstract method sends the event "startTransaction@S" to the server,
     * informing about the requested start of a new transaction
     *
     * @return Returns true if the start of the transaction was successful, else false
     */
    fun startTransaction(): Boolean

    /**
     * This abstract method sends the event "abortTransaction@S" to server,
     * informing about the abortion of the current transaction
     *
     */
    fun abortTransaction()

    /**
     * This abstract method sends the event "saveTransaction@S" to server,
     * informing about the requested ending of the current transaction
     * (therefore making the changes permanent)
     *
     */
    fun saveTransaction()

    /**
     * This abstract method sends the event "addSpontaneousSlot@S" with the duration and timeOfCreation
     * as data to the server, informing about the allocation of a new spontaneous Slot
     *
     * @param duration the duration of the new slot as Duration object
     * @param timeOfCreation the timestamp of the slot creation as DateTime object
     */
    fun addSpontaneousSlot(duration: Duration, timeOfCreation: DateTime)

    /**
     * This abstract method sends the event "addFixedSlot@S" with the duration and appointment time
     * as data to the server, informing about the allocation of a new fixed Slot
     *
     * @param duration the duration of the new slot as Duration object
     * @param appointmentTime the appointment time of the new slot as DateTime
     */
    fun addFixedSlot(duration: Duration, appointmentTime: DateTime)

    /**
     * This abstract method sends the event "deleteSlot@S" with the given slotCode as data
     * to the server, informing about the requested deletion of an existing slot
     *
     * @param slotCode as String, specifies the slot to be deleted
     */
    fun deleteSlot(slotCode: String)

    /**
     * This abstract method sends the event "endCurrentSlot@S" to the server, informing about
     * the completion / ending of the current slot
     *
     */
    fun endCurrentSlot()

    /**
     * This abstract method sends the "moveSlotAfterAnother@S" event with the given slotCodes as data
     * to the server, informing about the requested move / change of order for these slots
     *
     * @param movedSlot as String, specifies the slot which should be moved
     * @param otherSlot as String, specifies the slot, after which the moved slot should be placed
     */
    fun moveSlotAfterAnother(movedSlot: String, otherSlot: String)

    /**
     * This abstract method sends the event "changeSlotDuration@S" with slot code and new duration as
     * data to the server, informing about the request duration change of the given slot
     *
     * @param slotCode as String, specifies the slot, whose duration should be changed
     * @param newDuration as Duration object, specifies the requested new duration of the slot
     */
    fun changeSlotDuration(slotCode: String, newDuration: Duration)

    /**
     * This abstract method sends the event "changeFixedSlotTime@S" with slot code and new appointment time
     * to the server, informing about a requested change of apppointment time for the given slot
     *
     * @param slotCode as String, specifies the slot whose appointment time is to be changed
     * @param newTime as DateTime, th new appointment time for the given slot
     */
    fun changeFixedSlotTime(slotCode: String, newTime: DateTime)

    /**
     * Abstract getter for the received and currently valid WaitingQueue as observable
     * LiveData
     *
     * @return current WaitingQueue as LiveData of (encapsulated) type ReceivedList
     */
    fun getReceivedList(): LiveData<ReceivedList>

    /**
     * Abstract getter for the received and currently valid ManagementSettings as observable
     * LiveData
     *
     * @return current ManagementSettings as LiveData of (encapsulated) type Preferences
     */
    fun getUpdatedPreferences(): LiveData<Preferences>

    /**
     * Abstract getter for LiveData, that provides the client repository (or higher tier)
     * with error messages, that may be time-displaced or unexpectedly received by
     * the server, as observable LiveData
     *
     * @return list of (encapsulated) type ClientServerErrors, as LiveData
     */
    fun getErrors() : LiveData<List<ManagementServerErrors>>

}
