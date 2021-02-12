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

//TODO: Update Management und Slot zusammenstellen
class DatabaseWrapper (private val updateMediator: UpdateMediator){
    private lateinit var connection: Connection
    private val connectionProps: Properties = Properties()


    //TODO: Sicherstellen dass geladen
    init {
        this.connectionProps["user"] = "outwait"
        this.connectionProps["password"] = "OurOutwaitDB"
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", connectionProps)!!
            println("Connected to Database!")
        } catch (e : SQLException) {
            e.printStackTrace()
        }
    }


    fun getSlots(queueId: QueueId): List<Slot> {
        val getSlotsQuery: PreparedStatement
        val slots = mutableListOf<Slot>()
        try {
            //TODO: connection null abfangen
            getSlotsQuery = connection.prepareStatement(
                "SELECT code, duration, priority, init_time, eta"
                    + "FROM Slot"
                    + "WHERE Slot.queue_id = ?"
            )
            //TODO: toInt() oder in DB queueId auf Long setzen?
            getSlotsQuery.setInt(1, queueId.id.toInt())
            val rs = getSlotsQuery.executeQuery()
            while (rs.next()) {
                val s = Slot(
                    slotCode = SlotCode(code = rs.getString("code")),
                    constructorTime = Date(rs.getTimestamp("init_time").time),
                    approxTime = Date(rs.getTimestamp("eta").time),
                    expectedDuration = Duration.ofSeconds(rs.getInt("duration").toLong()),
                    priority = when (rs.getInt("priority")) {
                        0 -> Priority.URGENT
                        1 -> Priority.FIX_APPOINTMENT
                        else -> Priority.NORMAL
                    })
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
            //TODO: connection null abfangen
            getSlotApproxQuery = connection.prepareStatement(
                "SELECT eta"
                    + "FROM Slot"
                    + "WHERE Slot.code = ?"
            )
            getSlotApproxQuery.setString(1, slotCode.code)
            val rs = getSlotApproxQuery.executeQuery()
            slotApprox = Date(rs.getTimestamp("eta").time)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return slotApprox
    }

    fun setSlotApprox(slotCode: SlotCode, slotApprox: Date) {
        val setSlotApproxQuery: PreparedStatement
        try {
            //TODO: connection null abfangen
            setSlotApproxQuery = connection.prepareStatement(
                "UPDATE Slot"
                    + "SET eta = ?"
                    + "WHERE Slot.code = ?"
            )
            setSlotApproxQuery.setTimestamp(1, Timestamp(slotApprox.time))
            setSlotApproxQuery.setString(2, slotCode.code)
            setSlotApproxQuery.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun addTemporarySlot(slot : Slot, queueId: QueueId): SlotCode {
        //insert
        return SlotCode("")
    }

    fun deleteAllTemporarySlots(queueId: QueueId) {
        //delete
    }

    fun saveSlots(slots: List<Slot>, queueId: QueueId) {
        //Update (autom. dass slot nicht mehr temporär)
        //TODO: Wie soll gespeichert werden? Update oder Insert? Was ist mit SlotCode
    }

    fun getManagementById(managementId: ManagementId): ManagementInformation {
        val getManagementByIdQuery: PreparedStatement
        //TODO: Null Fall?
        var managementInformation = ManagementInformation()
        try {
            //TODO: connection null abfangen
            getManagementByIdQuery = connection.prepareStatement(
                "SELECT name, mode, default_slot_duration, client_notification_time, delay_notification_time, max_slot_waiting_time"
                    + "FROM Management"
                    + "WHERE Management.id = ?"
            )
            //TODO: toInt() oder in DB queueId auf Long setzen?
            getManagementByIdQuery.setInt(1, managementId.id.toInt())
            val rs = getManagementByIdQuery.executeQuery()
           managementInformation = ManagementInformation(
               ManagementDetails(rs.getString("name")),
               ManagementSettings(
                   when (rs.getInt("mode")) {
                       0 -> Mode.ONE
                       else -> Mode.TWO
                   },
                   Duration.ofSeconds(rs.getInt("default_slot_duration").toLong()),
                   Duration.ofSeconds(rs.getInt("client_notification_time").toLong()),
                   Duration.ofSeconds(rs.getInt("delay_notification_time").toLong()),
                   Duration.ofSeconds(rs.getInt("max_slot_waiting_time").toLong())
               )
           )

        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return managementInformation
    }

    fun getQueueIdOfManagement(managementId: ManagementId) : QueueId {
        val getQueueIdOfManagementQuery: PreparedStatement
        //TODO: Null Fall?
        var queueId = QueueId(-1)
        try {
            //TODO: connection null abfangen
            getQueueIdOfManagementQuery = connection.prepareStatement(
                "SELECT queue_id"
                    + "FROM Queue"
                    + "WHERE Queue.management_id = ?"
            )
            //TODO: toInt() oder in DB queueId auf Long setzen?
            getQueueIdOfManagementQuery.setInt(1, managementId.id.toInt())
            val rs = getQueueIdOfManagementQuery.executeQuery()
            queueId = QueueId(rs.getInt("queue_id").toLong())
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return queueId
    }

    fun getManagementByUsername(username: String): ManagementCredentials {
        val getManagementByUsernameQuery: PreparedStatement
        //TODO: Null Fall?
        var managementCredentials= ManagementCredentials(ManagementId(0), "", "")
        try {
            //TODO: connection null abfangen
            getManagementByUsernameQuery = connection.prepareStatement(
                "SELECT id, username, password"
                    + "FROM Management"
                    + "WHERE Management.username = ?"
            )
            getManagementByUsernameQuery.setString(1, username)
            val rs = getManagementByUsernameQuery.executeQuery()
            //TODO: toInt() oder in DB queueId auf Long setzen?
            managementCredentials = ManagementCredentials(
                ManagementId(rs.getInt("id").toLong()),
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

    //TODO: Ungeschickt, dass slotCode aus dem receiver gezogen wird? UpdateMediator vielleicht nur receiver übergeben?
    fun unregisterReceiver(receiver: SlotInformationReceiver) {
        updateMediator.unsubscribeSlotInformationReceiver(receiver.slotCode, receiver)
    }

    fun changeManagementPassword(username: String, password: String) {
        val changeManagementPasswordQuery: PreparedStatement
        try {
            //TODO: connection null abfangen
            changeManagementPasswordQuery = connection.prepareStatement(
                "UPDATE Management"
                    + "SET password = ?"
                    + "WHERE Management.username = ?"
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
            //TODO: connection null abfangen
            endSlotQuery = connection.prepareStatement(
                "DELETE FROM Slot"
                    + "WHERE Slot.code = ?"
            )
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
            //TODO: connection null abfangen
            deleteSlotQuery = connection.prepareStatement(
                "DELETE FROM Slot"
                    + "WHERE Slot.code = ?"
            )
            deleteSlotQuery.setString(1, slotCode.code)
            deleteSlotQuery.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        updateMediator.deleteSlot(slotCode)
    }
}
