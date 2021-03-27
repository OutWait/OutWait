package edu.kit.outwait.server.core

import edu.kit.outwait.server.client.SlotInformationReceiver
import edu.kit.outwait.server.management.*
import edu.kit.outwait.server.slot.Priority
import edu.kit.outwait.server.slot.Slot
import edu.kit.outwait.server.slot.SlotCode
import java.sql.*
import java.time.Duration
import java.util.Date
import java.util.Properties

/**
 * This class encapsulates all communication to the Database.
 *
 * @property updateMediator reference to the updateMediator which redirects changes to Receivers
 *     (f.ex. for Slot-Changes).
 * @property connection holds connection.
 * @property connectionProps properties of connection.
 * @param dbName the name of the database to connect to.
 * @constructor Connects to the database with name dbName and address dbAddress.
 * @throws SQLException when the connection to the database failed.
 */
class DatabaseWrapper @Throws(SQLException::class) constructor(dbName : String, dbAddress: String) {
    private val updateMediator = UpdateMediator()
    private var connection: Connection
    private val connectionProps: Properties = Properties()
    private val LOG_ID = "DB"

    init {
        this.connectionProps["user"] = "outwait"
        this.connectionProps["password"] = "OurOutwaitDB"
        try {
            connection =
                DriverManager.getConnection(
                    "jdbc:mysql://" + dbAddress + ":3306/" + dbName,
                    connectionProps
                )!!
            Logger.info(LOG_ID, "Connected to Database")
        } catch (e : SQLException) {
            e.printStackTrace()
            Logger.error(LOG_ID, "Failed connecting to database. Server stopped.")
            throw e
        }
    }

    /**
     * Retrieves a list of slots of a specified queue from the database. F. ex. for the
     * initialization of a queue in the manager.
     *
     * @param queueId Id der Queue dessen Slots zurückgegeben werden.
     * @return List of Slots of Queue with queueId
     */
    fun getSlots(queueId: QueueId): List<Slot>? {
        try {
            val slots = mutableListOf<Slot>()
            val getSlotsQuery =
                connection.prepareStatement(
                    "SELECT code, expected_duration, priority, constructor_time, approx_time " +
                        "FROM Slot " + "WHERE Slot.queue_id = ? AND Slot.is_temporary = 0"
                )
            getSlotsQuery.setLong(1, queueId.id)
            val rs = getSlotsQuery.executeQuery()
            while (rs.next()) {
                val s =
                    Slot(
                        slotCode = SlotCode(code = rs.getString("code")),
                        constructorTime = Date(rs.getTimestamp("constructor_time").time),
                        approxTime = Date(rs.getTimestamp("approx_time").time),
                        expectedDuration =
                            Duration.ofMillis(rs.getInt("expected_duration").toLong()),
                        priority = Priority.valueOf(rs.getString("priority"))
                    )
                slots.add(s)
            }
            return slots
        } catch (e: SQLException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Retrieves the approximated "time of arrival" for a slot from the database.
     *
     * @param slotCode code of the slot which approximated "time of arrival" is retrieved.
     * @return approximated "time of arrival" in Date type,
     */
    private fun getSlotApprox(slotCode: SlotCode): Date? {
        try {
            val getSlotApproxQuery =
                connection.prepareStatement(
                    "SELECT approx_time " + "FROM Slot " +
                        "WHERE Slot.code = ? AND Slot.is_temporary = 0"
                )
            getSlotApproxQuery.setString(1, slotCode.code)
            val rs = getSlotApproxQuery.executeQuery()
            rs.next()
            return Date(rs.getTimestamp("approx_time").time)
        } catch (e: SQLException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Adds a new temporary Slot to a specified queue of a management. A temporary Slot, is a Slot
     * which is created during a transaction without saving the transaction. So that it is not shown
     * for other managements until the transaction is saved.
     *
     * @param slot temporary Slot to be added.
     * @param queueId Queue in which the Slot has to be added
     * @return slot of param with an assigned SlotCode.
     */
    fun addTemporarySlot(slot : Slot, queueId: QueueId) : Slot? {
        try {
            val generatedSlotCode = arrayOf("code")
            val addTemporarySlotQuery =
                connection.prepareStatement(
                    "INSERT INTO Slot" +
                        "(queue_id, priority, approx_time, expected_duration, constructor_time, " +
                        "is_temporary) " + "VALUES(?, ?, ?, ?, ?, ?)",
                    generatedSlotCode
                )
            addTemporarySlotQuery.setLong(1, queueId.id)
            addTemporarySlotQuery.setString(2, slot.priority.toString())
            addTemporarySlotQuery.setTimestamp(3, Timestamp(slot.approxTime.time))
            addTemporarySlotQuery.setLong(4, slot.expectedDuration.toMillis())
            addTemporarySlotQuery.setTimestamp(5, Timestamp(slot.constructorTime.time))
            addTemporarySlotQuery.setInt(6, 1)
            addTemporarySlotQuery.executeUpdate()
            val keys = addTemporarySlotQuery.getGeneratedKeys()
            keys.next()
            val slotId = keys.getLong(1)
            Logger.debug(LOG_ID, "Inserted new slot with slotId:" + slotId)
            //Temporary fix
            val getSlotCodeQuery =
                connection.prepareStatement("SELECT code " + "FROM Slot " + "WHERE Slot.id = ?")
            getSlotCodeQuery.setLong(1, slotId)
            val rs = getSlotCodeQuery.executeQuery()
            rs.next()
            val slotCopy = slot.copy(slotCode = SlotCode(rs.getString("code")))
            Logger.debug(LOG_ID, "Returning inserted Slot with Slotcode" + slotCopy.slotCode.code)
            return slotCopy
        } catch (e: SQLException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Deletes all temporary Slots of a specific Queue. F. ex. after a transaction is aborted.
     *
     * @param queueId Id of specified Queue
     * @return true if successful else false
     */
    fun deleteAllTemporarySlots(queueId: QueueId): Boolean {
        val deleteAllTemporarySlotsUpdate: PreparedStatement
        try {
            deleteAllTemporarySlotsUpdate =
                connection.prepareStatement(
                    "DELETE FROM Slot " + "WHERE queue_id = ? AND is_temporary = 1"
                )
            deleteAllTemporarySlotsUpdate.setLong(1, queueId.id)
            deleteAllTemporarySlotsUpdate.executeUpdate()
            return true
        } catch (e: SQLException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Saves (updates) all slots with their slot data in the Slot entries of the database. All
     * temporary slots will be fixed! F. ex. after a transaction is saved.
     *
     * @param slots list of slots to be saved (updated)
     * @param queueId Queue in which the Slot has to be saved (updated)
     * @return true if successful else false
     */
    fun saveSlots(slots: List<Slot>, queueId: QueueId): Boolean {
        try {
            var saveSlotsUpdate: PreparedStatement
            for (slot in slots) {
                saveSlotsUpdate =
                    connection.prepareStatement(
                        "UPDATE Slot " +
                            "SET expected_duration = ?, priority = ?, approx_time = ?, " +
                            "is_temporary = 0 " + "WHERE queue_id = ? AND code = ?"
                    )
                saveSlotsUpdate.setLong(1, slot.expectedDuration.toMillis())
                saveSlotsUpdate.setString(2, slot.priority.toString())
                saveSlotsUpdate.setTimestamp(3, Timestamp(slot.approxTime.time))
                saveSlotsUpdate.setLong(4, queueId.id)
                saveSlotsUpdate.setString(5, slot.slotCode.code)
                saveSlotsUpdate.executeUpdate()
                updateMediator.setSlotApprox(slot.slotCode, slot.approxTime)
            }
            return true
        } catch (e: SQLException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Retrieves ManagementInformation by managementId from Database.
     *
     * @param managementId id of management
     * @return ManagementInformation with information about management
     */
    fun getManagementById(managementId: ManagementId): ManagementInformation? {
        try {
            val getManagementByIdQuery =
                connection.prepareStatement(
                    "SELECT name, email, mode, default_slot_duration, notification_time, " +
                        "delay_notification_time, prioritization_time " + "FROM Management " +
                        "WHERE Management.id = ?"
                )
            getManagementByIdQuery.setLong(1, managementId.id)
            val rs = getManagementByIdQuery.executeQuery()
            rs.next()
            return ManagementInformation(
                ManagementDetails(rs.getString("name"), rs.getString("Management.email")),
                ManagementSettings(
                    Mode.valueOf(rs.getString("mode")),
                    Duration.ofMillis(rs.getLong("default_slot_duration")),
                    Duration.ofMillis(rs.getLong("notification_time")),
                    Duration.ofMillis(rs.getLong("delay_notification_time")),
                    Duration.ofMillis(rs.getLong("prioritization_time"))
                )
            )
        } catch (e: SQLException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Retrieves SlotManagementInformation for a Slot by its Code from Database.
     *
     * @param slotCode code of Slot
     * @return SlotManagementInformation with information about management of Slot
     */
    fun getSlotManagementInformation(slotCode: SlotCode) : SlotManagementInformation? {
        try {
            val getSlotManagementInformationQuery =
                connection.prepareStatement(
                    "SELECT Management.name, Management.email, Management.notification_time, " +
                        "Management.delay_notification_time " + "FROM Slot " +
                        "INNER JOIN Queue ON Slot.queue_id = Queue.queue_id " +
                        "INNER JOIN Management ON Queue.management_id = Management.id " +
                        "WHERE Slot.code = ?"
                )
            getSlotManagementInformationQuery.setString(1, slotCode.code)
            val rs = getSlotManagementInformationQuery.executeQuery()
            rs.next()
            return SlotManagementInformation(
                ManagementDetails(rs.getString("name"), rs.getString("email")),
                Duration.ofMillis(rs.getLong("notification_time")),
                Duration.ofMillis(rs.getLong("delay_notification_time"))
            )
        } catch (e: SQLException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Retrieves QueueId for specified managementId from Database. Note: This only works, for
     * managements with one Queue.
     *
     * @param managementId Id of Management
     * @return QueueId
     */
    fun getQueueIdOfManagement(managementId: ManagementId) : QueueId? {
        try {
            val getQueueIdOfManagementQuery =
                connection.prepareStatement(
                    "SELECT queue_id " + "FROM Queue " + "WHERE Queue.management_id = ?"
                )
            getQueueIdOfManagementQuery.setLong(1, managementId.id)
            val rs = getQueueIdOfManagementQuery.executeQuery()
            rs.next()
            return QueueId(rs.getLong("queue_id"))
        } catch (e: SQLException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Retrieves credentials of management specified by username from Database
     *
     * @param username Username of specified management
     * @return ManagementCredentials of specified management
     */
    fun getManagementByUsername(username: String): ManagementCredentials? {
        try {
            val getManagementByUsernameQuery =
                connection.prepareStatement(
                    "SELECT id, username, password " + "FROM Management " +
                        "WHERE Management.username = ?"
                )
            getManagementByUsernameQuery.setString(1, username)
            val rs = getManagementByUsernameQuery.executeQuery()
            if (!rs.next()) return null // invalid username
            return ManagementCredentials(
                ManagementId(rs.getLong("id")),
                rs.getString("username"),
                rs.getString("password")
            )
        } catch (e: SQLException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Verifies that a Slot with specified SlotCode exists in Database. Helper method for
     * registerReceiver method.
     *
     * @param slotCode SlotCode of Slot
     * @return true if Slot exists else false
     */
    private fun checkIfSlotExists (slotCode: SlotCode) : Boolean {
        try {
            val checkIfSlotExistsQuery =
                connection.prepareStatement("SELECT code " + "FROM Slot " + "WHERE Slot.code = ?")
            checkIfSlotExistsQuery.setString(1, slotCode.code)
            val rs = checkIfSlotExistsQuery.executeQuery()
            return rs.next()
        } catch (e: SQLException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Saves (updates) settings for a management with specified managementId in Database.
     *
     * @param managementId Id of management to be updated
     * @param managementSettings new managementSetting for management
     * @return true if successful else false
     */
    fun saveManagementSettings(managementId: ManagementId, managementSettings: ManagementSettings):
        Boolean {
            try {
                val saveManagementSettingsUpdate =
                    connection.prepareStatement(
                        "UPDATE Management " +
                            "SET mode = ?, default_slot_duration = ?, notification_time = ?, " +
                            "delay_notification_time = ?, prioritization_time = ? " + "WHERE id = ?"
                    )
                saveManagementSettingsUpdate.setString(1, managementSettings.mode.toString())
                saveManagementSettingsUpdate.setLong(
                    2,
                    managementSettings.defaultSlotDuration.toMillis()
                )
                saveManagementSettingsUpdate.setLong(
                    3,
                    managementSettings.notificationTime.toMillis()
                )
                saveManagementSettingsUpdate.setLong(
                    4,
                    managementSettings.delayNotificationTime.toMillis()
                )
                saveManagementSettingsUpdate.setLong(
                    5,
                    managementSettings.prioritizationTime.toMillis()
                )
                saveManagementSettingsUpdate.setLong(6, managementId.id)
                saveManagementSettingsUpdate.executeUpdate()

                val getSlotsByManagementIdQuery =
                    connection.prepareStatement(
                        "SELECT Slot.code " + "FROM Management " +
                            "INNER JOIN Queue ON Management.id = Queue.management_id " +
                            "INNER JOIN Slot on Queue.queue_id = Slot.queue_id " +
                            "WHERE Management.id = ?"
                    )
                getSlotsByManagementIdQuery.setLong(1, managementId.id)
                val rs = getSlotsByManagementIdQuery.executeQuery()
                val slotCodes = mutableListOf<SlotCode>()
                while (rs.next()) {
                    slotCodes.add(SlotCode(rs.getString("code")))
                }
                if (!slotCodes.isEmpty()) {
                    val slotManagementInformation =
                        this.getSlotManagementInformation(slotCodes.first())
                    if (slotManagementInformation != null) {
                        updateMediator.setManagementInformation(
                            slotCodes,
                            slotManagementInformation
                        )
                    } else {
                        return false
                    }
                }
                return true
            } catch (e: SQLException) {
                e.printStackTrace()
                return false
            }
        }

    /**
     * Registers (SlotInformation)receiver in updateMediator. Calls getSlotApprox,
     * getSlotManagementInformation method for specified Slot(code) and passes results to the
     * updateMediator. Called after Client listens to a Slot.
     *
     * @param receiver SlotInformationReceiver to be registered
     * @param slotCode SlotCode of SlotInformationReceivers Slot
     * @return true if successful else false
     */
    fun registerReceiver(receiver: SlotInformationReceiver, slotCode: SlotCode): Boolean {
        if (checkIfSlotExists(slotCode)) {
            val slotApprox = this.getSlotApprox(slotCode)
            val slotManagementInformation = this.getSlotManagementInformation(slotCode)
            if (slotApprox == null || slotManagementInformation == null) {
                return false
            } else {
                updateMediator.subscribeReceiver(
                    receiver,
                    slotCode,
                    slotApprox,
                    slotManagementInformation
                )
                return true
            }
        } else {
            return false
        }
    }

    /**
     * Unregisters (SlotInformation)receiver in updateMediator. Called after Client stops listening
     * to a Slot (f.ex. after disconnect)
     *
     * @param receiver SlotInformationReceiver to be registered
     * @return true if successful else false
     */
    fun unregisterReceiver(receiver: SlotInformationReceiver) {
        updateMediator.unsubscribeSlotInformationReceiver(receiver)
    }

    /**
     * Changes password for a management specified by username in Database.
     *
     * @param username username of management
     * @param password new password to be assigned
     * @return true if successful else false
     */
    fun changeManagementPassword(username: String, password: String): Boolean {
        try {
            val changeManagementPasswordQuery =
                connection.prepareStatement(
                    "UPDATE Management " + "SET password = ? " + "WHERE Management.username = ?"
                )
            changeManagementPasswordQuery.setString(1, password)
            changeManagementPasswordQuery.setString(2, username)
            changeManagementPasswordQuery.executeUpdate()
            return true
        } catch (e: SQLException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Deletes a Slot from Database by specified SlotCode and calls endSlot with SlotCode on
     * updateMediator.
     *
     * @param slotCode SlotCode of Slot
     * @return true if successful else false
     */
    fun endSlot(slotCode: SlotCode): Boolean {
        val endSlotQuery: PreparedStatement
        try {
            endSlotQuery = connection.prepareStatement("DELETE FROM Slot " + "WHERE Slot.code = ?")
            endSlotQuery.setString(1, slotCode.code)
            endSlotQuery.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
            return false
        }
        updateMediator.endSlot(slotCode)
        return true
    }

    /**
     * Deletes a Slot from Database by specified SlotCode and calls deleteSlot with SlotCode on
     * updateMediator.
     *
     * @param slotCode SlotCode of Slot
     * @return true if successful else false
     */
    fun deleteSlot(slotCode: SlotCode): Boolean {
        val deleteSlotQuery: PreparedStatement
        try {
            deleteSlotQuery =
                connection.prepareStatement("DELETE FROM Slot " + "WHERE Slot.code = ?")
            deleteSlotQuery.setString(1, slotCode.code)
            deleteSlotQuery.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
            return false
        }
        updateMediator.deleteSlot(slotCode)
        return true
    }
}
