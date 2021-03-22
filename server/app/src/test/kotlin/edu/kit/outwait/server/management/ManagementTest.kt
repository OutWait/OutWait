package edu.kit.outwait.server.management

import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.protocol.*
import edu.kit.outwait.server.slot.*
import edu.kit.outwait.server.socketHelper.SocketFacade
import io.mockk.*
import java.time.Duration
import java.util.Date
import kotlin.test.assertEquals
import org.junit.jupiter.api.*

class ManagementTest {
    val db = mockk<DatabaseWrapper>()
    val socket = mockk<SocketFacade>()
    val mMgr = mockk<ManagementManager>()

    private fun createTimedSlot(
        code: String,
        constructorTimeEpochMs: Long,
        fixedSlot: Boolean = false,
        durationMinutes: Long = 30
    ) : Slot {
        return Slot(
            SlotCode(code),
            if (fixedSlot) Priority.FIX_APPOINTMENT else Priority.NORMAL,
            Date(constructorTimeEpochMs),
            Duration.ofMinutes(durationMinutes),
            Date(constructorTimeEpochMs)
        )
    }

    val testQueue =
        listOf(
            createTimedSlot("000000000", Date().getTime()),
            createTimedSlot("111111111", Date().getTime() + Duration.ofMinutes(30).toMillis()),
            createTimedSlot("222222222", Date().getTime() + Duration.ofMinutes(60).toMillis()),
            createTimedSlot(
                "333333333",
                Date().getTime() + Duration.ofMinutes(150).toMillis(),
                true
            )
        )

    val outSlots = slot<List<Slot>>()
    val beginTransactionCall = slot<(receivedData: JSONObjectWrapper) -> Unit>()
    val saveTransactionCall = slot<(receivedData: JSONObjectWrapper) -> Unit>()

    @BeforeEach
    fun prepareDB() {
        every { db.getManagementById(any()) } returns
            ManagementInformation(
                ManagementDetails("", ""),
                ManagementSettings(
                    Mode.TWO,
                    Duration.ZERO,
                    Duration.ZERO,
                    Duration.ZERO,
                    Duration.ZERO
                )
            )
        every { db.getQueueIdOfManagement(any()) } returns null
        every { db.getQueueIdOfManagement(ManagementId(1)) } returns QueueId(1)
        every { db.addTemporarySlot(any(), any()) } answers {
            arg<Slot>(0).copy(slotCode=SlotCode("333333333"))
        }
        every { db.endSlot(any()) } returns true
        every { db.deleteSlot(any()) } returns true

        // Used indirectly
        every { db.getSlots(any()) } returns testQueue
        every { db.saveSlots(capture(outSlots), any()) } returns true

        outSlots.captured = listOf<Slot>() // Clear output
    }

    @BeforeEach
    fun prepareSocket() {
        every { socket.onReceive(any(), any()) } just Runs
        every { socket.onDisconnect(any()) } just Runs
        every { socket.send(any(), any()) } just Runs

        every { socket.onReceive(Event.START_TRANSACTION, capture(beginTransactionCall)) } just Runs
        every { socket.onReceive(Event.SAVE_TRANSACTION, capture(saveTransactionCall)) } just Runs
    }

    @BeforeEach
    fun prepareManagementManager() {
        every { mMgr.beginTransaction(any()) } returns Queue(QueueId(1), db)
        every { mMgr.saveTransaction(any(), any()) } answers { arg<Queue>(1).storeToDB(db); Unit }
    }

    /** Checks whether creation of a management works properly. */
    @Test
    fun createManagement() {
        Management(socket, ManagementId(1), db, mMgr)

        verify(exactly = 1) {
            socket.send(Event.UPDATE_QUEUE, any())
            socket.send(Event.UPDATE_MANAGEMENT_SETTINGS, any())
        }
    }

    /** Checks whether creation of a management can fail. */
    @Test
    fun createInvalidManagement() {
        // ID 2 simulates a corrupted db
        Management(socket, ManagementId(2), db, mMgr)

        verify(exactly = 1) {
            socket.send(Event.UPDATE_MANAGEMENT_SETTINGS, any())
            socket.send(Event.INTERNAL_SERVER_ERROR, any())
        }
        verify(exactly = 0) { socket.send(Event.UPDATE_QUEUE, any()) }
    }

    /** Checks whether logout works. */
    @Test
    fun logout() {
        every { mMgr.removeManagement(any()) } just Runs
        val logoutCall = slot<(receivedData: JSONObjectWrapper) -> Unit>()
        every { socket.onReceive(Event.MANAGEMENT_LOGOUT, capture(logoutCall)) } just Runs
        every { socket.disconnect() } just Runs

        val management = Management(socket, ManagementId(1), db, mMgr)
        logoutCall.captured(JSONEmptyWrapper())

        verify(exactly = 1) {
            socket.send(Event.UPDATE_QUEUE, any())
            mMgr.removeManagement(management)
            socket.disconnect()
        }
    }

    /** Checks whether transaction beginning and saving works. */
    @Test
    fun saveTransaction() {
        Management(socket, ManagementId(1), db, mMgr)
        beginTransactionCall.captured(JSONEmptyWrapper())
        saveTransactionCall.captured(JSONEmptyWrapper())

        verifyOrder() {
            socket.send(Event.UPDATE_QUEUE, any())
            socket.send(Event.TRANSACTION_STARTED, any())
            mMgr.saveTransaction(ManagementId(1), any())
        }
    }

    /** Checks whether transaction beginning and aborting works. */
    @Test
    fun abortTransaction() {
        every { mMgr.abortTransaction(any()) } returns Queue(QueueId(1), db)
        val abortTransactionCall = slot<(receivedData: JSONObjectWrapper) -> Unit>()
        every { socket.onReceive(Event.ABORT_TRANSACTION, capture(abortTransactionCall)) } just Runs

        Management(socket, ManagementId(1), db, mMgr)
        beginTransactionCall.captured(JSONEmptyWrapper())
        abortTransactionCall.captured(JSONEmptyWrapper())

        verifyOrder() {
            socket.send(Event.UPDATE_QUEUE, any())
            socket.send(Event.TRANSACTION_STARTED, any())
            mMgr.abortTransaction(ManagementId(1))
            socket.send(Event.UPDATE_QUEUE, any())
        }
    }

    /** Checks whether starting a transaction can fail properly. */
    @Test
    fun startTransactionFail() {
        every { mMgr.beginTransaction(any()) } returns null // This will trigger the failure

        Management(socket, ManagementId(1), db, mMgr)
        beginTransactionCall.captured(JSONEmptyWrapper())

        verifyOrder() {
            socket.send(Event.UPDATE_QUEUE, any())
            socket.send(Event.TRANSACTION_DENIED, any())
        }
    }

    /** Checks whether starting a second transaction can fail properly. */
    @Test
    fun startTransactionAgainFail() {
        Management(socket, ManagementId(1), db, mMgr)
        beginTransactionCall.captured(JSONEmptyWrapper())
        beginTransactionCall.captured(JSONEmptyWrapper()) // second call will fail

        verifyOrder() {
            socket.send(Event.UPDATE_QUEUE, any())
            socket.send(Event.TRANSACTION_STARTED, any())
            socket.send(Event.INVALID_MANAGEMENT_REQUEST, any())
        }
    }

    /** Checks whether saving a transaction can fail properly. */
    @Test
    fun saveTransactionFail() {
        Management(socket, ManagementId(1), db, mMgr)
        saveTransactionCall.captured(JSONEmptyWrapper())
        // Will fail, because the transaction has not jet been started

        verifyOrder() {
            socket.send(Event.UPDATE_QUEUE, any())
            socket.send(Event.INVALID_MANAGEMENT_REQUEST, any())
        }
    }

    /** Checks whether aborting a transaction can fail properly. */
    @Test
    fun abortTransactionFail() {
        every { mMgr.abortTransaction(any()) } returns Queue(QueueId(1), db)
        val abortTransactionCall = slot<(receivedData: JSONObjectWrapper) -> Unit>()
        every { socket.onReceive(Event.ABORT_TRANSACTION, capture(abortTransactionCall)) } just Runs

        Management(socket, ManagementId(1), db, mMgr)
        abortTransactionCall.captured(JSONEmptyWrapper())
        // Will fail, because the transaction has not jet been started

        verifyOrder() {
            socket.send(Event.UPDATE_QUEUE, any())
            socket.send(Event.INVALID_MANAGEMENT_REQUEST, any())
        }
    }

    /** Checks whether adding a spontaneous slot works. */
    @Test
    fun addSpontaneousSlot() {
        val addSlot = slot<(receivedData: JSONObjectWrapper) -> Unit>()
        every { socket.onReceive(Event.ADD_SPONTANEOUS_SLOT, capture(addSlot)) } just Runs

        Management(socket, ManagementId(1), db, mMgr)
        beginTransactionCall.captured(JSONEmptyWrapper())
        val json = JSONAddSpontaneousSlotWrapper()
        json.setCreationTime(Date())
        json.setDuration(Duration.ofMinutes(30))
        addSlot.captured(json)
        saveTransactionCall.captured(JSONEmptyWrapper())

        verifyOrder() {
            socket.send(Event.UPDATE_QUEUE, any())
            socket.send(Event.TRANSACTION_STARTED, any())
            db.addTemporarySlot(any(), any())
            socket.send(Event.UPDATE_QUEUE, any())
            mMgr.saveTransaction(ManagementId(1), any())
            db.saveSlots(any(), any())
        }

        // Check if one was added. The queue algorithm is not tested here.
        assertEquals(testQueue.size + 1, outSlots.captured.size)
    }

    /** Checks whether adding a fix slot works. */
    @Test
    fun addFixSlot() {
        val addSlot = slot<(receivedData: JSONObjectWrapper) -> Unit>()
        every { socket.onReceive(Event.ADD_FIXED_SLOT, capture(addSlot)) } just Runs

        Management(socket, ManagementId(1), db, mMgr)
        beginTransactionCall.captured(JSONEmptyWrapper())
        val json = JSONAddFixedSlotWrapper()
        json.setAppointmentTime(Date())
        json.setDuration(Duration.ofMinutes(30))
        addSlot.captured(json)
        saveTransactionCall.captured(JSONEmptyWrapper())

        verifyOrder() {
            socket.send(Event.UPDATE_QUEUE, any())
            socket.send(Event.TRANSACTION_STARTED, any())
            db.addTemporarySlot(any(), any())
            socket.send(Event.UPDATE_QUEUE, any())
            mMgr.saveTransaction(ManagementId(1), any())
            db.saveSlots(any(), any())
        }

        // Check if one was added. The queue algorithm is not tested here.
        assertEquals(testQueue.size + 1, outSlots.captured.size)
    }

    /** Checks whether adding a slot from to far in the past fails. */
    @Test
    fun addSlotFromPast() {
        val addSlot = slot<(receivedData: JSONObjectWrapper) -> Unit>()
        every { socket.onReceive(Event.ADD_SPONTANEOUS_SLOT, capture(addSlot)) } just Runs

        Management(socket, ManagementId(1), db, mMgr)
        beginTransactionCall.captured(JSONEmptyWrapper())
        val json = JSONAddSpontaneousSlotWrapper()
        json.setCreationTime(Date.from(Date().toInstant() - Duration.ofHours(1)))
        json.setDuration(Duration.ofMinutes(30))
        addSlot.captured(json)

        verifyOrder() {
            socket.send(Event.UPDATE_QUEUE, any())
            socket.send(Event.INVALID_MANAGEMENT_REQUEST, any())
        }
    }

    /** Checks whether adding a slot from to far in the future fails. */
    @Test
    fun addSlotFromToFarFuture() {
        val addSlot = slot<(receivedData: JSONObjectWrapper) -> Unit>()
        every { socket.onReceive(Event.ADD_SPONTANEOUS_SLOT, capture(addSlot)) } just Runs

        Management(socket, ManagementId(1), db, mMgr)
        beginTransactionCall.captured(JSONEmptyWrapper())
        val json = JSONAddSpontaneousSlotWrapper()
        json.setCreationTime(
            Date.from(Date().toInstant() + Duration.ofHours(24) + Duration.ofMinutes(1))
        )
        json.setDuration(Duration.ofMinutes(30))
        addSlot.captured(json)

        verifyOrder() {
            socket.send(Event.UPDATE_QUEUE, any())
            socket.send(Event.INVALID_MANAGEMENT_REQUEST, any())
        }
    }

    /** Checks whether ending the current slot works. */
    @Test
    fun endSlot() {
        val endSlot = slot<(receivedData: JSONObjectWrapper) -> Unit>()
        every { socket.onReceive(Event.END_CURRENT_SLOT, capture(endSlot)) } just Runs

        Management(socket, ManagementId(1), db, mMgr)
        beginTransactionCall.captured(JSONEmptyWrapper())
        endSlot.captured(JSONEmptyWrapper())
        saveTransactionCall.captured(JSONEmptyWrapper())

        verifyOrder() {
            socket.send(Event.UPDATE_QUEUE, any())
            socket.send(Event.TRANSACTION_STARTED, any())
            socket.send(Event.UPDATE_QUEUE, any())
            mMgr.saveTransaction(ManagementId(1), any())
            db.saveSlots(any(), any())
            db.endSlot(any())
        }

        // Check if one was removed. The queue algorithm is not tested here.
        assertEquals(testQueue.size - 1, outSlots.captured.size)
    }

    /** Checks whether deleting a slot works. */
    @Test
    fun deleteSlot() {
        val deleteSlot = slot<(receivedData: JSONObjectWrapper) -> Unit>()
        every { socket.onReceive(Event.DELETE_SLOT, capture(deleteSlot)) } just Runs

        Management(socket, ManagementId(1), db, mMgr)
        beginTransactionCall.captured(JSONEmptyWrapper())
        val json = JSONSlotCodeWrapper()
        json.setSlotCode(SlotCode("111111111"))
        deleteSlot.captured(json)
        saveTransactionCall.captured(JSONEmptyWrapper())

        verifyOrder() {
            socket.send(Event.UPDATE_QUEUE, any())
            socket.send(Event.TRANSACTION_STARTED, any())
            socket.send(Event.UPDATE_QUEUE, any())
            mMgr.saveTransaction(ManagementId(1), any())
            db.saveSlots(any(), any())
            db.deleteSlot(any())
        }

        // Check if one was removed. The queue algorithm is not tested here.
        assertEquals(testQueue.size - 1, outSlots.captured.size)
    }

    /** Checks whether moving a slot after another works. */
    @Test
    fun moveSlot() {
        val moveSlot = slot<(receivedData: JSONObjectWrapper) -> Unit>()
        every { socket.onReceive(Event.MOVE_SLOT_AFTER_ANOTHER, capture(moveSlot)) } just Runs

        Management(socket, ManagementId(1), db, mMgr)
        beginTransactionCall.captured(JSONEmptyWrapper())
        val json = JSONSlotMovementWrapper()
        json.setMovedSlot(SlotCode("111111111"))
        json.setOtherSlot(SlotCode("222222222"))
        moveSlot.captured(json)
        saveTransactionCall.captured(JSONEmptyWrapper())

        verifyOrder() {
            socket.send(Event.UPDATE_QUEUE, any())
            socket.send(Event.TRANSACTION_STARTED, any())
            socket.send(Event.UPDATE_QUEUE, any())
            mMgr.saveTransaction(ManagementId(1), any())
            db.saveSlots(any(), any())
        }

        // Check if the right one was moved. The queue algorithm is not tested here.
        assertEquals(testQueue.size, outSlots.captured.size)
        assertEquals(SlotCode("111111111"), outSlots.captured[2].slotCode)
    }

    /** Checks whether changing the time of a slot works. */
    @Test
    fun changeSlotTime() {
        val changeSlotTime = slot<(receivedData: JSONObjectWrapper) -> Unit>()
        every { socket.onReceive(Event.CHANGE_FIXED_SLOT_TIME, capture(changeSlotTime)) } just Runs

        Management(socket, ManagementId(1), db, mMgr)
        beginTransactionCall.captured(JSONEmptyWrapper())
        val targetTime = Date.from(testQueue[2].approxTime.toInstant() + Duration.ofMinutes(100))
        val json = JSONChangeSlotTimeWrapper()
        json.setSlotCode(SlotCode("333333333"))
        json.setNewTime(targetTime)
        changeSlotTime.captured(json)
        saveTransactionCall.captured(JSONEmptyWrapper())

        verifyOrder() {
            socket.send(Event.UPDATE_QUEUE, any())
            socket.send(Event.TRANSACTION_STARTED, any())
            socket.send(Event.UPDATE_QUEUE, any())
            mMgr.saveTransaction(ManagementId(1), any())
            db.saveSlots(any(), any())
        }

        // Check if the time was changed. The queue algorithm is not tested here.
        assertEquals(testQueue.size, outSlots.captured.size)
        assertEquals(targetTime, outSlots.captured[3].constructorTime)
    }

    /** Checks whether changing the time of a slot works. */
    @Test
    fun changeSlotLength() {
        val changeSlotLength = slot<(receivedData: JSONObjectWrapper) -> Unit>()
        every { socket.onReceive(Event.CHANGE_SLOT_DURATION, capture(changeSlotLength)) } just Runs

        Management(socket, ManagementId(1), db, mMgr)
        beginTransactionCall.captured(JSONEmptyWrapper())
        val targetDuration = Duration.ofMinutes(11)
        val json = JSONChangeSlotDurationWrapper()
        json.setSlotCode(SlotCode("333333333"))
        json.setNewDuration(targetDuration)
        changeSlotLength.captured(json)
        saveTransactionCall.captured(JSONEmptyWrapper())

        verifyOrder() {
            socket.send(Event.UPDATE_QUEUE, any())
            socket.send(Event.TRANSACTION_STARTED, any())
            socket.send(Event.UPDATE_QUEUE, any())
            mMgr.saveTransaction(ManagementId(1), any())
            db.saveSlots(any(), any())
        }

        // Check if the duration was changed. The queue algorithm is not tested here.
        assertEquals(testQueue.size, outSlots.captured.size)
        assertEquals(targetDuration, outSlots.captured[3].expectedDuration)
    }

    /** Checks whether changing the settings works. */
    @Test
    fun changeSettings() {
        every { mMgr.updateManagementSettings(any(), any()) } just Runs
        val changeSettings = slot<(receivedData: JSONObjectWrapper) -> Unit>()
        every { socket.onReceive(Event.CHANGE_MANAGEMENT_SETTINGS, capture(changeSettings)) } just
            Runs

        Management(socket, ManagementId(1), db, mMgr)
        val json = JSONManagementSettingsWrapper()
        json.setSettings(
            ManagementSettings(Mode.ONE, Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO)
        )
        changeSettings.captured(json)

        verifyOrder() {
            socket.send(Event.UPDATE_QUEUE, any())
            mMgr.updateManagementSettings(any(), any())
        }
    }
}
