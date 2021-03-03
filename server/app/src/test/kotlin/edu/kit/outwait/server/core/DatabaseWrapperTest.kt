package edu.kit.outwait.server.core

import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.core.Logger
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
            val deleteSlotsUpdate =
                connection.prepareStatement(
                    "DELETE FROM Slot"
                )
            deleteSlotsUpdate.executeUpdate()

            val addSlotsUpdate =
                connection.prepareStatement(
                    "INSERT INTO Slot (code, queue_id, expected_duration, priority, constructor_time, " +
                        "approx_time, id, is_temporary) VALUES ('TEST_TEST', 1, 1500000, 'NORMAL', 2021-03-01 16:13:07, " +
                        "2021-03-01 19:48:07, 999, 0)"
                )


            deleteSlotsUpdate.executeUpdate()
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
        assertEquals(slots.isNullOrEmpty(), false )
        val testSlot = slots!![0]
        assertEquals(testSlot.slotCode.code, "TEST_TEST")
        assertEquals(testSlot.expectedDuration.toMillis(), 1500000)
        assertEquals(testSlot.approxTime.time, Timestamp.valueOf("2021-03-01 19:48:07"))
        assertEquals(testSlot.constructorTime.time, Timestamp.valueOf("2021-03-01 16:13:07"))
        assertEquals(testSlot.priority.name, "NORMAL")
    }
}
