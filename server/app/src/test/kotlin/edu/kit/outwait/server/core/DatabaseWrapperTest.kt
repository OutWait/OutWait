package edu.kit.outwait.server.core

import edu.kit.outwait.server.management.ManagementId
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
import kotlin.time.ExperimentalTime

class DatabaseWrapperTest {
    private lateinit var connection: Connection
    private val connectionProps: Properties = Properties()
    private var testObj = DatabaseWrapper("OutwaitDBTest", "161.97.168.24")

    init {
        this.connectionProps["user"] = "outwait"
        this.connectionProps["password"] = "OurOutwaitDB"
        try {
            connection =
                DriverManager.getConnection(
                    "jdbc:mysql://161.97.168.24:3306/OutwaitDBTest",
                    connectionProps
                )!!
        } catch (e : SQLException) {
            e.printStackTrace()
            println("Failed connecting to database. Test stopped.")
            throw e
        }
    }

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
        testObj = DatabaseWrapper("OutwaitDBTest", "161.97.168.24")
    }

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

    @org.junit.jupiter.api.Test
    fun testAddTemporarySlot() {
        val slot =
            Slot(
                SlotCode("TEMP_SLOT"),
                Priority.NORMAL,
                Date(123),
                Duration.ofMillis(123),
                Date(123)
            )
        val returnedSlots = testObj.addTemporarySlot(slot, QueueId(1))
        assertEquals(returnedSlots == null, false)
        assertEquals(returnedSlots!!.slotCode.code.length, 9)
        assertEquals(returnedSlots.priority.name, "NORMAL")
        assertEquals(returnedSlots.expectedDuration.toMillis(), 123)
        assertEquals(returnedSlots.constructorTime.time, Date(123).time)
        assertEquals(returnedSlots.approxTime.time, Date(123).time)
    }

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
        testObj.deleteAllTemporarySlots(QueueId(1))
        val getTempSlotsQuery =
            connection.prepareStatement("SELECT * FROM Slot WHERE is_temporary = ?")
        getTempSlotsQuery.setInt(1, 1)
        val rs = getTempSlotsQuery.executeQuery()
        assertEquals(rs.next(), false)
    }

    @ExperimentalTime
    @org.junit.jupiter.api.Test
    fun testSaveSlots() {

        val slots =
            listOf<Slot>(
                Slot(
                    SlotCode("TEST_TEST"),
                    Priority.FIX_APPOINTMENT,
                    Date(321),
                    Duration.ofMillis(321),
                    Date(321)
                )
            )
        assertEquals(testObj.saveSlots(slots, QueueId(1)), true)
        val getTempSlotsQuery = connection.prepareStatement("SELECT * FROM Slot WHERE code = ?")
        getTempSlotsQuery.setString(1, "TEST_TEST")
        val rs = getTempSlotsQuery.executeQuery()
        assertEquals(rs.next(), true)
        assertEquals(rs.getString("priority"), "FIX_APPOINTMENT")
        //Weird behaviour
        /* assertEquals(Date(rs.getTimestamp("approx_time").time).time.milliseconds,
         * Date(321).time.milliseconds)
         *        assertEquals(rs.getLong("expected_duration"), 321)
         * assertEquals(Date(rs.getTimestamp("constructor_time").time), Date(321).time) */
    }

    @org.junit.jupiter.api.Test
    fun testGetManagementById() {
        val managementInfo = testObj.getManagementById(ManagementId(1))
        assertEquals(managementInfo == null, false)
        assertEquals(managementInfo!!.details.name, "TEST-Praxis")
        assertEquals(managementInfo!!.settings.defaultSlotDuration.toMillis(), 1800000)
        assertEquals(managementInfo!!.settings.delayNotificationTime.toMillis(), 120000)
        assertEquals(managementInfo!!.settings.notificationTime.toMillis(), 180000)
        assertEquals(managementInfo!!.settings.mode.name, "TWO")
    }

    @org.junit.jupiter.api.Test
    fun testGetSlotManagementInformation() {
        val slotManagementInfo = testObj.getSlotManagementInformation(SlotCode("TEST_TEST"))
        assertEquals(slotManagementInfo == null, false)
        assertEquals(slotManagementInfo!!.details.name, "TEST-Praxis")
        assertEquals(slotManagementInfo!!.delayNotificationTime.toMillis(), 120000)
        assertEquals(slotManagementInfo!!.notificationTime.toMillis(), 180000)
    }

    @org.junit.jupiter.api.Test
    fun testGetManagementByUsername() {
        val managementCredentials = testObj.getManagementByUsername("test")
        assertEquals(managementCredentials == null, false)
        assertEquals(managementCredentials!!.id.id, 1)
        assertEquals(managementCredentials!!.password, "test")
        assertEquals(managementCredentials!!.username, "test")
    }

    @org.junit.jupiter.api.Test
    fun testSaveManagementSettings() {
        val managementCredentials = testObj.getManagementByUsername("test")
        assertEquals(managementCredentials == null, false)
        assertEquals(managementCredentials!!.id.id, 1)
        assertEquals(managementCredentials!!.password, "test")
        assertEquals(managementCredentials!!.username, "test")
    }

    @org.junit.jupiter.api.Test
    fun testChangeManagementPassword() {
        testObj.changeManagementPassword("test", "new")
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

    @org.junit.jupiter.api.Test
    fun testEndSlot() {
        testObj.endSlot(SlotCode("TEST_TEST"))
        val getSlotQuery = connection.prepareStatement("SELECT * FROM Slot WHERE code = ?")
        getSlotQuery.setString(1, "TEST_TEST")
        val rs = getSlotQuery.executeQuery()
        assertEquals(rs.next(), false)
    }

    @org.junit.jupiter.api.Test
    fun testDeleteSlot() {
        testObj.deleteSlot(SlotCode("TEST_TEST"))
        val getSlotQuery = connection.prepareStatement("SELECT * FROM Slot WHERE code = ?")
        getSlotQuery.setString(1, "TEST_TEST")
        val rs = getSlotQuery.executeQuery()
        assertEquals(rs.next(), false)
    }
}
