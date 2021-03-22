package edu.kit.outwait.server.management

import com.corundumstudio.socketio.AckRequest
import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIONamespace
import com.corundumstudio.socketio.listener.ConnectListener
import com.corundumstudio.socketio.listener.DataListener
import com.corundumstudio.socketio.listener.DisconnectListener
import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.protocol.*
import edu.kit.outwait.server.slot.*
import io.mockk.*
import jakarta.mail.Transport
import java.time.Duration
import java.util.Date
import java.util.UUID
import org.junit.jupiter.api.*

class ManagementManagerTest {
    val db = mockk<DatabaseWrapper>()
    val namespace = mockk<SocketIONamespace>()
    val socketClient = mockk<SocketIOClient>()
    val socketClientB = mockk<SocketIOClient>()
    var connectionStarted = false;
    var connectionStartedB = false;
    val ackRequest = mockk<AckRequest>()

    val onConnectListener = slot<ConnectListener>()
    val onEventListener = hashMapOf<Event, CapturingSlot<DataListener<String>>>()
    val onDisconnectListener = slot<DisconnectListener>()

    val testQueue =
        listOf(Slot(SlotCode("000000000"), Priority.NORMAL, Date(), Duration.ofMinutes(30), Date()))

    @BeforeEach
    fun prepareDB() {
        every { db.getManagementByUsername("test") } returns
            ManagementCredentials(ManagementId(1), "test", "test")
        every { db.getManagementByUsername("invalid") } returns null
        every { db.getManagementByUsername("invalidMailUser") } returns
            ManagementCredentials(ManagementId(2), "test2", "test2")
        every { db.getManagementById(any()) } returns
            ManagementInformation(
                ManagementDetails("", "email@example.com"),
                ManagementSettings(
                    Mode.TWO,
                    Duration.ZERO,
                    Duration.ZERO,
                    Duration.ZERO,
                    Duration.ZERO
                )
            )
        every { db.getManagementById(ManagementId(2)) } returns
            ManagementInformation(
                ManagementDetails("", "invalid"),
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
        every { db.getSlots(any()) } returns testQueue
        every { db.saveSlots(any(), any()) } returns true
        every { db.deleteAllTemporarySlots(any()) } returns true
        every { db.saveManagementSettings(any(), any()) } returns true
        every { db.changeManagementPassword(any(), any()) } returns true
    }

    @BeforeEach
    fun prepareSocketIO() {
        every { namespace.addConnectListener(capture(onConnectListener)) } just Runs
        for (e in Event.values()) {
            var slot = CapturingSlot<DataListener<String>>()
            every {
                namespace.addEventListener<String>(
                    e.getEventTag(),
                    String::class.java,
                    capture(slot)
                )
            } just Runs
            onEventListener[e] = slot
        }
        every { namespace.addDisconnectListener(capture(onDisconnectListener)) } just Runs

        every { socketClient.getSessionId() } returns UUID(0, 1)
        every { socketClient.sendEvent(any(), *anyVararg()) } just Runs
        every { socketClient.isChannelOpen() } returns connectionStarted
        every { socketClient.disconnect() } answers { connectionStarted = false; Unit }

        every { socketClientB.getSessionId() } returns UUID(0, 2)
        every { socketClientB.sendEvent(any(), *anyVararg()) } just Runs
        every { socketClientB.isChannelOpen() } returns connectionStartedB
        every { socketClientB.disconnect() } answers { connectionStartedB = false; Unit }
    }

    fun loginRoutine() {
        val json = JSONCredentialsWrapper();
        json.setUsername("test")
        json.setPassword("test")
        onEventListener[Event.MANAGEMENT_LOGIN]!!.captured
            .onData(socketClient, json.getJSONString(), ackRequest)
    }

    fun loginRoutineB() {
        val json = JSONCredentialsWrapper();
        json.setUsername("test")
        json.setPassword("test")
        onEventListener[Event.MANAGEMENT_LOGIN]!!.captured
            .onData(socketClientB, json.getJSONString(), ackRequest)
    }

    fun logoutRoutine() {
        onEventListener[Event.MANAGEMENT_LOGOUT]!!.captured
            .onData(socketClient, JSONEmptyWrapper().getJSONString(), ackRequest)
    }

    fun logoutRoutineB() {
        onEventListener[Event.MANAGEMENT_LOGOUT]!!.captured
            .onData(socketClientB, JSONEmptyWrapper().getJSONString(), ackRequest)
    }

    /** Checks whether creation of the manager works properly (and doesn't crash). */
    @Test
    fun createManager() {
        ManagementManager(namespace, db)
    }

    /** Checks whether login and logout works. */
    @Test
    fun login() {
        ManagementManager(namespace, db)
        connectionStarted = true
        onConnectListener.captured.onConnect(socketClient)

        // Login
        loginRoutine()

        verifyOrder {
            socketClient.sendEvent(Event.MANAGEMENT_LOGIN_SUCCESS.getEventTag(), *anyVararg())
            socketClient.sendEvent(Event.UPDATE_MANAGEMENT_SETTINGS.getEventTag(), *anyVararg())
            socketClient.sendEvent(Event.UPDATE_QUEUE.getEventTag(), *anyVararg())
        }

        // Logout again
        logoutRoutine()
    }

    /** Checks whether login with empty name fails. */
    @Test
    fun invalidLogin() {
        ManagementManager(namespace, db)
        connectionStarted = true
        onConnectListener.captured.onConnect(socketClient)

        // Login
        val json = JSONCredentialsWrapper();
        json.setUsername("")
        json.setPassword("invalid")
        onEventListener[Event.MANAGEMENT_LOGIN]!!.captured
            .onData(socketClient, json.getJSONString(), ackRequest)

        verify { socketClient.sendEvent(Event.MANAGEMENT_LOGIN_DENIED.getEventTag(), *anyVararg()) }

        verify(exactly = 0) {
            socketClient.sendEvent(Event.MANAGEMENT_LOGIN_SUCCESS.getEventTag(), *anyVararg())
            socketClient.sendEvent(Event.UPDATE_MANAGEMENT_SETTINGS.getEventTag(), *anyVararg())
            socketClient.sendEvent(Event.UPDATE_QUEUE.getEventTag(), *anyVararg())
        }

        // Logout again
        logoutRoutine()
    }

    /** Checks whether login with wrong password fails. */
    @Test
    fun invalidLoginPassword() {
        ManagementManager(namespace, db)
        connectionStarted = true
        onConnectListener.captured.onConnect(socketClient)

        // Login
        val json = JSONCredentialsWrapper();
        json.setUsername("test")
        json.setPassword("invalid")
        onEventListener[Event.MANAGEMENT_LOGIN]!!.captured
            .onData(socketClient, json.getJSONString(), ackRequest)

        verify { socketClient.sendEvent(Event.MANAGEMENT_LOGIN_DENIED.getEventTag(), *anyVararg()) }

        verify(exactly = 0) {
            socketClient.sendEvent(Event.MANAGEMENT_LOGIN_SUCCESS.getEventTag(), *anyVararg())
            socketClient.sendEvent(Event.UPDATE_MANAGEMENT_SETTINGS.getEventTag(), *anyVararg())
            socketClient.sendEvent(Event.UPDATE_QUEUE.getEventTag(), *anyVararg())
        }

        // Logout again
        logoutRoutine()
    }

    /** Checks whether starting a transaction works. */
    @Test
    fun beginTransaction() {
        ManagementManager(namespace, db)
        connectionStarted = true
        onConnectListener.captured.onConnect(socketClient)
        loginRoutine()

        onEventListener[Event.START_TRANSACTION]!!.captured
            .onData(socketClient, JSONEmptyWrapper().getJSONString(), ackRequest)

        verify { socketClient.sendEvent(Event.TRANSACTION_STARTED.getEventTag(), *anyVararg()) }

        // Logout again
        logoutRoutine()
    }

    /** Checks whether starting a transaction can fail. */
    @Test
    fun beginTransactionFail() {
        ManagementManager(namespace, db)
        connectionStarted = true
        onConnectListener.captured.onConnect(socketClient)
        loginRoutine()
        // Open another connection
        onConnectListener.captured.onConnect(socketClientB)
        loginRoutineB()

        onEventListener[Event.START_TRANSACTION]!!.captured
            .onData(socketClient, JSONEmptyWrapper().getJSONString(), ackRequest)
        // Second start will trigger the failure
        onEventListener[Event.START_TRANSACTION]!!.captured
            .onData(socketClientB, JSONEmptyWrapper().getJSONString(), ackRequest)

        verifyOrder {
            socketClient.sendEvent(Event.TRANSACTION_STARTED.getEventTag(), *anyVararg())
            socketClientB.sendEvent(Event.TRANSACTION_DENIED.getEventTag(), *anyVararg())
        }

        verify(exactly = 0) {
            socketClientB.sendEvent(Event.TRANSACTION_STARTED.getEventTag(), *anyVararg())
        }

        // Logout again
        logoutRoutine()
        logoutRoutineB()
    }

    /** Checks whether saving a transaction works. */
    @Test
    fun saveTransaction() {
        ManagementManager(namespace, db)
        connectionStarted = true
        onConnectListener.captured.onConnect(socketClient)
        loginRoutine()

        onEventListener[Event.START_TRANSACTION]!!.captured
            .onData(socketClient, JSONEmptyWrapper().getJSONString(), ackRequest)
        onEventListener[Event.SAVE_TRANSACTION]!!.captured
            .onData(socketClient, JSONEmptyWrapper().getJSONString(), ackRequest)

        verifyOrder {
            socketClient.sendEvent(Event.UPDATE_QUEUE.getEventTag(), *anyVararg())
            socketClient.sendEvent(Event.TRANSACTION_STARTED.getEventTag(), *anyVararg())
            socketClient.sendEvent(Event.UPDATE_QUEUE.getEventTag(), *anyVararg())
        }

        // Logout again
        logoutRoutine()
    }

    /** Checks whether aborting a transaction works. */
    @Test
    fun abortTransaction() {
        ManagementManager(namespace, db)
        connectionStarted = true
        onConnectListener.captured.onConnect(socketClient)
        loginRoutine()

        onEventListener[Event.START_TRANSACTION]!!.captured
            .onData(socketClient, JSONEmptyWrapper().getJSONString(), ackRequest)
        onEventListener[Event.ABORT_TRANSACTION]!!.captured
            .onData(socketClient, JSONEmptyWrapper().getJSONString(), ackRequest)

        verifyOrder {
            socketClient.sendEvent(Event.UPDATE_QUEUE.getEventTag(), *anyVararg())
            socketClient.sendEvent(Event.TRANSACTION_STARTED.getEventTag(), *anyVararg())
            socketClient.sendEvent(Event.UPDATE_QUEUE.getEventTag(), *anyVararg())
        }

        // Logout again
        logoutRoutine()
    }

    /** Checks whether changing the settings works. */
    @Test
    fun updateSettings() {
        ManagementManager(namespace, db)
        connectionStarted = true
        onConnectListener.captured.onConnect(socketClient)
        loginRoutine()

        val json = JSONManagementSettingsWrapper()
        json.setSettings(
            ManagementSettings(Mode.ONE, Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO)
        )
        onEventListener[Event.CHANGE_MANAGEMENT_SETTINGS]!!.captured
            .onData(socketClient, json.getJSONString(), ackRequest)

        verifyOrder {
            socketClient.sendEvent(Event.UPDATE_MANAGEMENT_SETTINGS.getEventTag(), *anyVararg())
            socketClient.sendEvent(Event.UPDATE_QUEUE.getEventTag(), *anyVararg())
            socketClient.sendEvent(Event.UPDATE_MANAGEMENT_SETTINGS.getEventTag(), *anyVararg())
        }

        // Logout again
        logoutRoutine()
    }

    /** Checks whether resetting the password works. */
    @Test
    fun resetPassword() {
        mockkStatic("jakarta.mail.Transport")
        every { Transport.send(any()) } just Runs

        ManagementManager(namespace, db)
        connectionStarted = true
        onConnectListener.captured.onConnect(socketClient)

        val json = JSONResetPasswordWrapper()
        json.setUsername("test")
        onEventListener[Event.RESET_PASSWORD]!!.captured
            .onData(socketClient, json.getJSONString(), ackRequest)

        verifyOrder { db.changeManagementPassword(any(), any()) }
    }

    /** Checks whether resetting the password of an invalid user fails. */
    @Test
    fun resetPasswordInvalidUser() {
        mockkStatic("jakarta.mail.Transport")
        every { Transport.send(any()) } just Runs

        ManagementManager(namespace, db)
        connectionStarted = true
        onConnectListener.captured.onConnect(socketClient)

        val json = JSONResetPasswordWrapper()
        json.setUsername("invalid")
        onEventListener[Event.RESET_PASSWORD]!!.captured
            .onData(socketClient, json.getJSONString(), ackRequest)

        verify(exactly = 0) { db.changeManagementPassword(any(), any()) }
    }

    /** Checks whether resetting the password over an invalid email fails. */
    @Test
    fun resetPasswordInvalidEmail() {
        mockkStatic("jakarta.mail.Transport")
        every { Transport.send(any()) } just Runs

        ManagementManager(namespace, db)
        connectionStarted = true
        onConnectListener.captured.onConnect(socketClient)

        val json = JSONResetPasswordWrapper()
        json.setUsername("invalidMailUser")
        onEventListener[Event.RESET_PASSWORD]!!.captured
            .onData(socketClient, json.getJSONString(), ackRequest)

        verify(exactly = 0) { db.changeManagementPassword(any(), any()) }
    }

    /** Checks the delay update routine works. */
    @Test
    fun queueDelay() {
        val mMgr = ManagementManager(namespace, db)
        val queue = mockk<Queue>();
        every { queue.updateQueue(any()) } just Runs
        every { queue.calculateNextDelayChange() } returns
            Date.from(Date().toInstant() + Duration.ofSeconds(3))
        every { queue.storeToDB(any()) } returns true

        mMgr.beginTransaction(ManagementId(1))
        mMgr.saveTransaction(ManagementId(1), queue)

        verifyOrder { queue.calculateNextDelayChange() }
        verify(exactly = 0) { db.saveSlots(any(), any()) }

        Thread.sleep(4000)
        // After the update, the following should has been called
        verifyOrder { db.saveSlots(any(), any()) }
    }
}
