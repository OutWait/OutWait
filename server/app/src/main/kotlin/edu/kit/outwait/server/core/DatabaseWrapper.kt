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
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.time.Duration
import java.util.Date
import java.util.Properties
import java.sql.PreparedStatement



class DatabaseWrapper (private val updateMediator: UpdateMediator){
    private var connection: Connection? = null
    private val connectionProps: Properties = Properties()


    init {
        this.connectionProps["user"] = "outwait"
        this.connectionProps["password"] = "OurOutwaitDB"
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", connectionProps)
            println("Connected to Database!")
        } catch (e : SQLException) {
            e.printStackTrace()
        }
    }

    fun getSlots(queueId: QueueId): List<Slot> {
        val notificationTimeQuery: PreparedStatement
        val slots = mutableListOf<Slot>()
        try {
            notificationTimeQuery = connection!!.prepareStatement(
                "SELECT code, duration, priority, init_time, eta"
                    + "FROM Slot"
                    + "WHERE Slot.queue_id = ?"
            )
            notificationTimeQuery.setString(1, queueId.id.toString())
            val rs = notificationTimeQuery.executeQuery()
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
        return Date(0)
    }

    fun setSlotApprox(slotCode: SlotCode, slotApprox: Date) {
    }

    fun saveSlots(slots: List<Slot>, queueId: QueueId) {
    }

    fun getManagementById(managementId: ManagementId): ManagementInformation {
        return ManagementInformation(
            ManagementDetails(""),
            ManagementSettings(Mode.ONE, Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO)
        )
    }

    fun getQueueIdOfManagement(managementId: ManagementId) : QueueId {
        return QueueId(0);
    }

    fun getManagementByUsername(username: String): ManagementCredentials {
        return ManagementCredentials(ManagementId(0), "", "")
    }

    fun saveManagementSettings(managementSettings: ManagementSettings) {
    }

    fun registerReceiver(receiver: SlotInformationReceiver, slotCode: SlotCode): Boolean {
        return true
    }

    fun unregisterReceiver(receiver: SlotInformationReceiver) {
    }

    fun changeManagementPassword(username: String, password: String) {
    }

    fun endSlot(slotCode: SlotCode) {
    }

    fun deleteSlot(slotCode: SlotCode) {
    }
}
