package edu.kit.outwait.server.client

import com.corundumstudio.socketio.SocketIONamespace
import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.slot.SlotCode
import edu.kit.outwait.server.socketHelper.SocketFacade
import io.mockk.*
import kotlin.test.assertEquals

/** Unit-Tests for ClientManager */
class ClientManagerTest {
    var namespaceMock = mockk<SocketIONamespace>(relaxed = true)
    var databaseWrapperMock = mockk<DatabaseWrapper>(relaxed = true)
    var testObj = ClientManager(namespaceMock, databaseWrapperMock)

    /**
     * Creates mock-objects for namespace and databaseWrapper with relaxed mode on since they're
     * needed to instantiate new ClientManagerObject
     */
    @org.junit.jupiter.api.BeforeEach
    fun setUp() {
        namespaceMock = mockk<SocketIONamespace>(relaxed = true)
        databaseWrapperMock = mockk<DatabaseWrapper>(relaxed = true)
        testObj = ClientManager(namespaceMock, databaseWrapperMock)
    }

    /** Checks if Client has been initialized by verifying onDisconnect call. */
    @org.junit.jupiter.api.Test
    fun testBindSocket() {
        val socketFacadeMock = mockk<SocketFacade>(relaxed = true)
        testObj.bindSocket(socketFacadeMock)
        verify { socketFacadeMock.onDisconnect(any()) }
    }

    /** Checks if unregisterReceiver is called with correct parameters on databaseWrapper */
    @org.junit.jupiter.api.Test
    fun testRemoveReceiver() {
        val receiverCapturer = CapturingSlot<SlotInformationReceiver>()
        val receiverMock = mockk<SlotInformationReceiver>()
        every { databaseWrapperMock.unregisterReceiver(capture(receiverCapturer)) } just runs
        testObj.removeReceiver(receiverMock)
        verify { databaseWrapperMock.unregisterReceiver(receiverMock) }
        assertEquals(receiverMock, receiverCapturer.captured)
    }

    /** Checks if registerReceiver is called with correct parameters on databaseWrapper */
    @org.junit.jupiter.api.Test
    fun testRegisterReceiver() {
        val slotCodeCapturer = CapturingSlot<SlotCode>()
        val receiverCapturer = CapturingSlot<SlotInformationReceiver>()
        val slotCodeMock = mockk<SlotCode>()
        val receiverMock = mockk<SlotInformationReceiver>()
        every {
            databaseWrapperMock.registerReceiver(
                capture(receiverCapturer),
                capture(slotCodeCapturer)
            )
        } returns true
        testObj.registerReceiver(slotCodeMock, receiverMock)
        verify { databaseWrapperMock.registerReceiver(receiverMock, slotCodeMock) }
        assertEquals(slotCodeMock, slotCodeCapturer.captured)
        assertEquals(receiverMock, receiverCapturer.captured)
    }
}
