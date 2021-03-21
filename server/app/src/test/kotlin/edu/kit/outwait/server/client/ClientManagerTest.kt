package edu.kit.outwait.server.client

import com.corundumstudio.socketio.SocketIONamespace
import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.protocol.Event
import edu.kit.outwait.server.protocol.JSONSlotCodeWrapper
import edu.kit.outwait.server.slot.SlotCode
import edu.kit.outwait.server.socketHelper.SocketFacade
import io.mockk.*
import kotlin.test.assertEquals

class ClientManagerTest {
    var namespaceMock = mockk<SocketIONamespace>(relaxed = true)
    var databaseWrapperMock = mockk<DatabaseWrapper>(relaxed = true)
    var testObj = ClientManager(namespaceMock, databaseWrapperMock)

    @org.junit.jupiter.api.BeforeEach
    fun setUp() {
        var namespaceMock = mockk<SocketIONamespace>(relaxed = true)
        var databaseWrapperMock = mockk<DatabaseWrapper>(relaxed = true)
        var testObj = ClientManager(namespaceMock, databaseWrapperMock)
    }

    @org.junit.jupiter.api.Test
    internal fun testBindSocket() {
    //?
    }

    @org.junit.jupiter.api.Test
    internal fun testRemoveClient() {
        //?
    }

    @org.junit.jupiter.api.Test
    internal fun testRemoveReceiver() {
        val receiverCapturer = CapturingSlot<SlotInformationReceiver>()
        val receiverMock = mockk<SlotInformationReceiver>()
        every { databaseWrapperMock.unregisterReceiver(capture(receiverCapturer))} just runs
        testObj.removeReceiver(receiverMock)
        verify { databaseWrapperMock.unregisterReceiver(receiverMock)}
        assertEquals(receiverMock, receiverCapturer.captured)

    }

    @org.junit.jupiter.api.Test
    internal fun testRegisterReceiver() {
        val slotCodeCapturer = CapturingSlot<SlotCode>()
        val receiverCapturer = CapturingSlot<SlotInformationReceiver>()
        val slotCodeMock = mockk<SlotCode>()
        val receiverMock = mockk<SlotInformationReceiver>()
        every { databaseWrapperMock.registerReceiver(capture(receiverCapturer), capture(slotCodeCapturer))} returns true
        testObj.registerReceiver(slotCodeMock, receiverMock)
        verify { databaseWrapperMock.registerReceiver(receiverMock, slotCodeMock)}
        assertEquals(slotCodeMock, slotCodeCapturer.captured)
        assertEquals(receiverMock, receiverCapturer.captured)

    }
}
