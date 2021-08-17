package edu.kit.outwait.server.core

import edu.kit.outwait.server.management.ManagementId
import edu.kit.outwait.server.management.ManagementSettings
import edu.kit.outwait.server.management.Mode
import edu.kit.outwait.server.management.QueueId
import edu.kit.outwait.server.slot.Priority
import edu.kit.outwait.server.slot.Slot
import edu.kit.outwait.server.slot.SlotCode
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Timestamp
import java.time.Duration
import java.util.*
import kotlin.test.assertEquals

/** Unit-Tests for DatabaseWrapper */
class DatabaseWrapperTest {
    private var connection: Connection
    private val connectionProps: Properties = Properties()
    private var testObj = DatabaseWrapper("OutwaitDBTest", "0.0.0.0") // TODO replace with server ip.

    init {
        this.connectionProps["user"] = "outwait"
        this.connectionProps["password"] = "OurOutwaitDB"
        try {
            connection =
                DriverManager.getConnection(
                    "jdbc:mysql://0.0.0.0:3306/OutwaitDBTest", // TODO replace with server ip.
                    connectionProps
                )!!
        } catch (e : SQLException) {
            e.printStackTrace()
            println("Failed connecting to database. Test stopped.")
            throw e
        }
    }

    /** Deletes all existing Slots and creates Testing Slot to be used on tests */
    @org.junit.jupiter.api.BeforeEach
    fun setUp() {
        try {
            val deleteSlotsUpdate = connection.prepareStatement("DELETE FROM Slot")
            deleteSlotsUpdate.executeUpdate()

            val addSlotsUpdate =
                connection.prepareStatement(
                    "INSERT INTO Slot (code, queue_id, expected_duration, priority, " +
                        "constructor_time, " +
                        "approx_time, id, is_temporary) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                )
            addSlotsUpdate.setString(1, "TEST_TEST")
            addSlotsUpdate.setLong(2, 1)
            addSlotsUpdate.setLong(3, 15000)
            addSlotsUpdate.setString(4, "NORMAL")
            addSlotsUpdate.setTimestamp(5, Timestamp.valueOf("2021-03-01 16:13:07"))
            addSlotsUpdate.setTimestamp(6, Timestamp.valueOf("2021-03-01 19:48:07"))
            addSlotsUpdate.setLong(7, 999)
            addSlotsUpdate.setInt(8, 0)
            addSlotsUpdate.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
            throw e
        }
        testObj = DatabaseWrapper("OutwaitDBTest", "0.0.0.0") // TODO replace with server ip.
    }

    /** Checks if getSlots method returns slot with properties of Test Slot */
    @org.junit.jupiter.api.Test
    fun testGetSlots() {
        val slots = testObj.getSlots(QueueId(1))
        println(slots.toString())
        assertEquals(slots.isNullOrEmpty(), false)
        val testSlot = slots!![0]
        assertEquals(testSlot.slotCode.code, "TEST_TEST")
        assertEquals(testSlot.expectedDuration.toMillis(), 15000)
        assertEquals(testSlot.approxTime.time, Timestamp.valueOf("2021-03-01 19:48:07").time)
        assertEquals(testSlot.constructorTime.time, Timestamp.valueOf("2021-03-01 16:13:07").time)
        assertEquals(testSlot.priority.name, "NORMAL")
    }

    /**
     * Checks if addTemporarySlot inserts slot correctly to Database and also returns returns
     * correctly.
     */
    @org.junit.jupiter.api.Test
    fun testAddTemporarySlot() {
        val slot =
            Slot(
                SlotCode("TEMP_SLOT"),
                Priority.NORMAL,
                Date(123000),
                Duration.ofMillis(123000),
                Date(123000)
            )
        val returnedSlots = testObj.addTemporarySlot(slot, QueueId(1))
        assertEquals(returnedSlots == null, false)
        assertEquals(returnedSlots!!.slotCode.code.length, 9)
        assertEquals(returnedSlots.priority.name, "NORMAL")
        assertEquals(returnedSlots.expectedDuration.toMillis(), 123000)
        assertEquals(returnedSlots.constructorTime.time, Date(123000).time)
        assertEquals(returnedSlots.approxTime.time, Date(123000).time)
        val getTempSlotsQuery =
            connection.prepareStatement("SELECT * FROM Slot WHERE is_temporary = 1")
        val rs = getTempSlotsQuery.executeQuery()
        assertEquals(rs.next(), true)
        assertEquals(rs.getString("code").length, 9)
        assertEquals(rs.getString("priority"), "NORMAL")
        assertEquals(rs.getLong("expected_duration"), 123000)
        assertEquals(Date(rs.getTimestamp("constructor_time").time).time, Date(123000).time)
        assertEquals(Date(rs.getTimestamp("approx_time").time).time, Date(123000).time)
    }

    /** Checks if temporary Slot is deleted with deleteAllTemporarySlots after insertion */
    @org.junit.jupiter.api.Test
    fun testdeleteAllTemporarySlot() {
        val addSlotsUpdate =
            connection.prepareStatement(
                "INSERT INTO Slot (code, queue_id, expected_duration, priority, constructor_time," +
                    " " + "approx_time, id, is_temporary) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            )
        addSlotsUpdate.setString(1, "TEMP_TEST")
        addSlotsUpdate.setLong(2, 1)
        addSlotsUpdate.setLong(3, 15000)
        addSlotsUpdate.setString(4, "NORMAL")
        addSlotsUpdate.setTimestamp(5, Timestamp.valueOf("2021-03-01 16:13:07"))
        addSlotsUpdate.setTimestamp(6, Timestamp.valueOf("2021-03-01 19:48:07"))
        addSlotsUpdate.setLong(7, 9999)
        addSlotsUpdate.setInt(8, 1)
        addSlotsUpdate.executeUpdate()
        val ret = testObj.deleteAllTemporarySlots(QueueId(1))
        assertEquals(true, ret)
        val getTempSlotsQuery =
            connection.prepareStatement("SELECT * FROM Slot WHERE is_temporary = ?")
        getTempSlotsQuery.setInt(1, 1)
        val rs = getTempSlotsQuery.executeQuery()
        assertEquals(rs.next(), false)
    }

    /** Checks if slot saved with saveSlots method by comparing properties */
    @org.junit.jupiter.api.Test
    fun testSaveSlots() {

        val slots =
            listOf<Slot>(
                Slot(
                    SlotCode("TEST_TEST"),
                    Priority.FIX_APPOINTMENT,
                    Date(321000),
                    Duration.ofMillis(321000),
                    Date(321000)
                )
            )
        assertEquals(testObj.saveSlots(slots, QueueId(1)), true)
        val getTempSlotsQuery = connection.prepareStatement("SELECT * FROM Slot WHERE code = ?")
        getTempSlotsQuery.setString(1, "TEST_TEST")
        val rs = getTempSlotsQuery.executeQuery()
        assertEquals(rs.next(), true)
        assertEquals(rs.getString("priority"), "FIX_APPOINTMENT")
        assertEquals(Date(rs.getTimestamp("approx_time").time).time, Date(321000).time)
        assertEquals(rs.getLong("expected_duration"), 321000)
        assertEquals(
            Date(rs.getTimestamp("constructor_time").time).time,
            Timestamp.valueOf("2021-03-01 16:13:07").time
        )
    }

    /** Checks if returned management of getManagementById method is correct */
    @org.junit.jupiter.api.Test
    fun testGetManagementById() {
        val managementInfo = testObj.getManagementById(ManagementId(1))
        assertEquals(managementInfo == null, false)
        assertEquals(managementInfo!!.details.name, "TEST-Praxis")
        assertEquals(managementInfo.settings.defaultSlotDuration.toMillis(), 1800000)
        assertEquals(managementInfo.settings.delayNotificationTime.toMillis(), 120000)
        assertEquals(managementInfo.settings.notificationTime.toMillis(), 180000)
        assertEquals(managementInfo.settings.mode.name, "TWO")
        assertEquals(managementInfo.details.email, "test@test.test")
    }

    /**
     * Checks if returned managementInformation of getSlotManagementInformation method is correct
     */
    @org.junit.jupiter.api.Test
    fun testGetSlotManagementInformation() {
        val slotManagementInfo = testObj.getSlotManagementInformation(SlotCode("TEST_TEST"))
        assertEquals(slotManagementInfo == null, false)
        assertEquals(slotManagementInfo!!.details.name, "TEST-Praxis")
        assertEquals(slotManagementInfo.details.email, "test@test.test")
        assertEquals(slotManagementInfo.delayNotificationTime.toMillis(), 120000)
        assertEquals(slotManagementInfo.notificationTime.toMillis(), 180000)
    }

    /** Checks if returned managementCredentials of getManagementByUsername method is correct */
    @org.junit.jupiter.api.Test
    fun testGetManagementByUsername() {
        val managementCredentials = testObj.getManagementByUsername("test")
        assertEquals(managementCredentials == null, false)
        assertEquals(managementCredentials!!.id.id, 1)
        assertEquals(managementCredentials.password, "test")
        assertEquals(managementCredentials.username, "test")
    }

    /**
     * Checks if returned managementCredentials of getManagementSe method is correct and sets
     * Settings of management back to default
     */
    @org.junit.jupiter.api.Test
    fun testSaveManagementSettings() {
        val newManagementSettings =
            ManagementSettings(
                Mode.ONE,
                Duration.ofMillis(123),
                Duration.ofMillis(123),
                Duration.ofMillis(123),
                Duration.ofMillis(123)
            )
        val ret = testObj.saveManagementSettings(ManagementId(1), newManagementSettings)
        assertEquals(true, ret)
        val getManagementSettingsQuery =
            connection.prepareStatement("SELECT * FROM Management WHERE username = ?")
        getManagementSettingsQuery.setString(1, "test")
        val rs = getManagementSettingsQuery.executeQuery()
        assertEquals(true, rs.next())
        assertEquals("ONE", rs.getString("mode"))
        assertEquals(123, rs.getLong("default_slot_duration"))
        assertEquals(123, rs.getLong("notification_time"))
        assertEquals(123, rs.getLong("prioritization_time"))
        assertEquals(123, rs.getLong("delay_notification_time"))

        val cleanManagementSettingsUpdate =
            connection.prepareStatement(
                "UPDATE Management " +
                    "SET mode=?, default_slot_duration=?, prioritization_time=?, " +
                    "notification_time=?, delay_notification_time=? " + "WHERE Management.id = ?"
            )
        cleanManagementSettingsUpdate.setString(1, "TWO")
        cleanManagementSettingsUpdate.setLong(2, 1800000)
        cleanManagementSettingsUpdate.setLong(3, 120000)
        cleanManagementSettingsUpdate.setLong(4, 180000)
        cleanManagementSettingsUpdate.setLong(5, 120000)
        cleanManagementSettingsUpdate.setLong(6, 1)
        cleanManagementSettingsUpdate.executeUpdate()
    }

    /**
     * Checks if password for management was changed correctly by changeManageamentPassword method
     */
    @org.junit.jupiter.api.Test
    fun testChangeManagementPassword() {
        val ret = testObj.changeManagementPassword("test", "new")
        assertEquals(true, ret)
        val getManagementPasswordQuery =
            connection.prepareStatement("SELECT password FROM Management WHERE username = ?")
        getManagementPasswordQuery.setString(1, "test")
        val rs = getManagementPasswordQuery.executeQuery()
        rs.next()
        assertEquals(rs.getString("password"), "new")
        val resetManagementPasswordUpdate =
            connection.prepareStatement("UPDATE Management SET password = ? WHERE username = ?")
        resetManagementPasswordUpdate.setString(1, "test")
        resetManagementPasswordUpdate.setString(2, "test")
        resetManagementPasswordUpdate.executeUpdate()
    }

    /** Checks if Slot is deleted from Database after calling endSlot method on it */
    @org.junit.jupiter.api.Test
    fun testEndSlot() {
        val ret = testObj.endSlot(SlotCode("TEST_TEST"))
        assertEquals(true, ret)
        val getSlotQuery = connection.prepareStatement("SELECT * FROM Slot WHERE code = ?")
        getSlotQuery.setString(1, "TEST_TEST")
        val rs = getSlotQuery.executeQuery()
        assertEquals(rs.next(), false)
    }

    /** Checks if Slot is deleted from Database after calling deleteSlot method on it */
    @org.junit.jupiter.api.Test
    fun testDeleteSlot() {
        val ret = testObj.deleteSlot(SlotCode("TEST_TEST"))
        assertEquals(true, ret)
        val getSlotQuery = connection.prepareStatement("SELECT * FROM Slot WHERE code = ?")
        getSlotQuery.setString(1, "TEST_TEST")
        val rs = getSlotQuery.executeQuery()
        assertEquals(rs.next(), false)
    }

    /** Checks if QueueId returned from getQueueIdOfManagement is correct */
    @org.junit.jupiter.api.Test
    fun testGetQueueIdOfManagement() {
        val queueId = testObj.getQueueIdOfManagement(ManagementId(1))
        assertEquals(1, queueId!!.id)
    }

    /** Checks if Slot is deleted from Database after calling deleteSlot method on it */
    @org.junit.jupiter.api.Test
    fun testRegisterReceiver() {
        val ret = testObj.deleteSlot(SlotCode("TEST_TEST"))
        assertEquals(true, ret)
        val getSlotQuery = connection.prepareStatement("SELECT * FROM Slot WHERE code = ?")
        getSlotQuery.setString(1, "TEST_TEST")
        val rs = getSlotQuery.executeQuery()
        assertEquals(rs.next(), false)
    }

    /** Checks if Slot is deleted from Database after calling deleteSlot method on it */
    @org.junit.jupiter.api.Test
    fun testUnregisterReceiver() {
        val ret = testObj.deleteSlot(SlotCode("TEST_TEST"))
        assertEquals(true, ret)
        val getSlotQuery = connection.prepareStatement("SELECT * FROM Slot WHERE code = ?")
        getSlotQuery.setString(1, "TEST_TEST")
        val rs = getSlotQuery.executeQuery()
        assertEquals(rs.next(), false)
    }
}
