package edu.kit.outwait.server.core

import edu.kit.outwait.server.client.SlotInformationReceiver
import edu.kit.outwait.server.management.ManagementCredentials
import edu.kit.outwait.server.management.ManagementDetails
import edu.kit.outwait.server.management.ManagementId
import edu.kit.outwait.server.management.ManagementInformation
import edu.kit.outwait.server.management.ManagementSettings
import edu.kit.outwait.server.management.Mode
import edu.kit.outwait.server.management.QueueId
import edu.kit.outwait.server.slot.Priority
import edu.kit.outwait.server.slot.Slot
import edu.kit.outwait.server.slot.SlotCode
import java.sql.*
import java.time.Duration
import java.util.Date
import java.util.Properties

class DatabaseWrapper() {
    private val updateMediator = UpdateMediator()
    private lateinit var connection: Connection
    private val connectionProps: Properties = Properties()

    //TODO: Sicherstellen dass geladen
    init {
        this.connectionProps["user"] = "outwait"
        this.connectionProps["password"] = "OurOutwaitDB"
        try {
            connection =
                DriverManager.getConnection("jdbc:mysql://localhost:3306/", connectionProps)!!
            println("Connected to Database!")
        } catch (e : SQLException) {
            e.printStackTrace()
        }
    }

    fun getSlots(queueId: QueueId): List<Slot> {
        val getSlotsQuery: PreparedStatement
        val slots = mutableListOf<Slot>()
        try {
            getSlotsQuery =
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
                            Duration.ofSeconds(rs.getInt("expected_duration").toLong()),
                        priority = Priority.valueOf(rs.getString("priority"))
                    )
                slots.add(s)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return slots
    }

    fun getSlotApprox(slotCode: SlotCode): Date {
        val getSlotApproxQuery: PreparedStatement
        var slotApprox = Date()
        try {
            getSlotApproxQuery =
                connection.prepareStatement(
                    "SELECT approx_time " + "FROM Slot" +
                        "WHERE Slot.code = ? AND Slot.is_temporary = 0"
                )
            getSlotApproxQuery.setString(1, slotCode.code)
            val rs = getSlotApproxQuery.executeQuery()
            slotApprox = Date(rs.getTimestamp("approx_time").time)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return slotApprox
    }

    fun setSlotApprox(slotCode: SlotCode, slotApprox: Date) {
        val setSlotApproxUpdate: PreparedStatement
        try {
            setSlotApproxUpdate =
                connection.prepareStatement(
                    "UPDATE Slot " + "SET approx_time = ? " + "WHERE Slot.code = ?"
                )
            setSlotApproxUpdate.setTimestamp(1, Timestamp(slotApprox.time))
            setSlotApproxUpdate.setString(2, slotCode.code)
            setSlotApproxUpdate.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun addTemporarySlot(slot : Slot, queueId: QueueId) : Slot {
        val addTemporarySlotQuery: PreparedStatement
        var slotCopy = slot.copy()
        try {
            addTemporarySlotQuery =
                connection.prepareStatement(
                    "INSERT INTO Slot " +
                        "(priority, approx_time, expected_duration, constructor_time, " +
                        "is_temporary) " + "OUTPUT INSERTED.code " + "VALUES (?, ?, ?, ?, ?)"
                )
            addTemporarySlotQuery.setString(1, slot.priority.toString())
            addTemporarySlotQuery.setTimestamp(2, Timestamp(slot.approxTime.time))
            addTemporarySlotQuery.setLong(3, slot.expectedDuration.seconds)
            addTemporarySlotQuery.setInt(4, 1)
            val rs = addTemporarySlotQuery.executeQuery()
            slotCopy = slot.copy(slotCode = SlotCode(rs.getString("code")))
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return slotCopy
    }

    fun deleteAllTemporarySlots(queueId: QueueId) {
        val deleteAllTemporarySlotsUpdate: PreparedStatement
        try {
            deleteAllTemporarySlotsUpdate =
                connection.prepareStatement(
                    "DELETE FROM Slot " + "WHERE queue_id = ? AND is_temporary = 1"
                )
            deleteAllTemporarySlotsUpdate.setLong(1, queueId.id)
            deleteAllTemporarySlotsUpdate.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun saveSlots(slots: List<Slot>, queueId: QueueId) {
        //Update (autom. dass slot nicht mehr temporär)
        var saveSlotsUpdate: PreparedStatement
        try {
            for (slot in slots) {
                saveSlotsUpdate =
                    connection.prepareStatement(
                        "UPDATE Slot " +
                            "SET expected_duration = ?, priority = ?, approx_time = ?, " +
                            "is_temporary = 0 " + "WHERE queue_id = ? AND code = ?"
                    )
                saveSlotsUpdate.setLong(1, slot.expectedDuration.seconds)
                saveSlotsUpdate.setString(2, slot.priority.toString())
                saveSlotsUpdate.setTimestamp(3, Timestamp(slot.approxTime.time))
                saveSlotsUpdate.setLong(4, queueId.id)
                saveSlotsUpdate.setString(5, slot.slotCode.code)
                saveSlotsUpdate.executeUpdate()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun getManagementById(managementId: ManagementId): ManagementInformation {
        val getManagementByIdQuery: PreparedStatement
        try {
            getManagementByIdQuery =
                connection.prepareStatement(
                    "SELECT name, mode, default_slot_duration, notification_time, " +
                        "delay_notification_time, prioritization_time " + "FROM Management " +
                        "WHERE Management.id = ?"
                )
            getManagementByIdQuery.setLong(1, managementId.id)
            val rs = getManagementByIdQuery.executeQuery()
            return ManagementInformation(
                ManagementDetails(rs.getString("name")),
                ManagementSettings(
                    Mode.valueOf(rs.getString("mode")),
                    Duration.ofSeconds(rs.getLong("default_slot_duration")),
                    Duration.ofSeconds(rs.getLong("notification_time")),
                    Duration.ofSeconds(rs.getLong("delay_notification_time")),
                    Duration.ofSeconds(rs.getLong("prioritization_time"))
                )
            )
        } catch (e: SQLException) {
            e.printStackTrace()
            // TODO return something useful on error (or handle the error in the calling functions)
            return ManagementInformation(
                ManagementDetails(""),
                ManagementSettings(
                    Mode.ONE,
                    Duration.ZERO,
                    Duration.ZERO,
                    Duration.ZERO,
                    Duration.ZERO
                )
            )
        }
    }

    fun getQueueIdOfManagement(managementId: ManagementId) : QueueId {
        val getQueueIdOfManagementQuery: PreparedStatement
        //TODO: Null Fall?
        var queueId = QueueId(-1)
        try {
            getQueueIdOfManagementQuery =
                connection.prepareStatement(
                    "SELECT queue_id" + "FROM Queue" + "WHERE Queue.management_id = ?"
                )
            getQueueIdOfManagementQuery.setLong(1, managementId.id)
            val rs = getQueueIdOfManagementQuery.executeQuery()
            queueId = QueueId(rs.getLong("queue_id"))
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return queueId
    }

    fun getManagementByUsername(username: String): ManagementCredentials {
        val getManagementByUsernameQuery: PreparedStatement
        //TODO: Null Fall?
        var managementCredentials = ManagementCredentials(ManagementId(0), "", "")
        try {
            getManagementByUsernameQuery =
                connection.prepareStatement(
                    "SELECT id, username, password" + "FROM Management" +
                        "WHERE Management.username = ?"
                )
            getManagementByUsernameQuery.setString(1, username)
            val rs = getManagementByUsernameQuery.executeQuery()
            managementCredentials =
                ManagementCredentials(
                    ManagementId(rs.getLong("id")),
                    rs.getString("username"),
                    rs.getString("password")
                )
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return managementCredentials
    }

    fun saveManagementSettings(managementSettings: ManagementSettings) {
        //TODO: UPDATE oder INSERT
    }

    fun registerReceiver(receiver: SlotInformationReceiver, slotCode: SlotCode): Boolean {
        return true
    }

    //TODO: Ungeschickt, dass slotCode aus dem receiver gezogen wird? UpdateMediator vielleicht nur
    // receiver übergeben?
    fun unregisterReceiver(receiver: SlotInformationReceiver) {
        updateMediator.unsubscribeSlotInformationReceiver(receiver.slotCode, receiver)
    }

    fun changeManagementPassword(username: String, password: String) {
        val changeManagementPasswordQuery: PreparedStatement
        try {
            changeManagementPasswordQuery =
                connection.prepareStatement(
                    "UPDATE Management" + "SET password = ?" + "WHERE Management.username = ?"
                )
            changeManagementPasswordQuery.setString(1, password)
            changeManagementPasswordQuery.setString(2, username)
            changeManagementPasswordQuery.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun endSlot(slotCode: SlotCode) {
        val endSlotQuery: PreparedStatement
        try {
            endSlotQuery = connection.prepareStatement("DELETE FROM Slot" + "WHERE Slot.code = ?")
            endSlotQuery.setString(1, slotCode.code)
            endSlotQuery.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        updateMediator.endSlot(slotCode)
    }

    fun deleteSlot(slotCode: SlotCode) {
        val deleteSlotQuery: PreparedStatement
        try {
            deleteSlotQuery =
                connection.prepareStatement("DELETE FROM Slot" + "WHERE Slot.code = ?")
            deleteSlotQuery.setString(1, slotCode.code)
            deleteSlotQuery.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        updateMediator.deleteSlot(slotCode)
    }
}
