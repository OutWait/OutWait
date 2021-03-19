package edu.kit.outwait.server.client

import edu.kit.outwait.server.management.ManagementDetails
import edu.kit.outwait.server.management.SlotManagementInformation
import edu.kit.outwait.server.slot.SlotCode
import edu.kit.outwait.server.socketHelper.SocketFacade
import edu.kit.outwait.server.protocol.Event
import edu.kit.outwait.server.protocol.JSONSlotCodeWrapper
import edu.kit.outwait.server.protocol.JSONSlotDataWrapper
import io.mockk.*
import java.sql.Date
import java.time.Duration
import kotlin.test.assertEquals

class ClientTest {
    var socketFacadeMock = mockk<SocketFacade>(relaxed = true)
    var clientManagerMock = mockk<ClientManager>(relaxed = true)
    var testObj = Client(socketFacadeMock, clientManagerMock)

    @org.junit.jupiter.api.BeforeEach
    fun setUp() {
        socketFacadeMock = mockk<SocketFacade>(relaxed = true)
        clientManagerMock = mockk<ClientManager>(relaxed = true)
        testObj = Client(socketFacadeMock, clientManagerMock)
    }

    @org.junit.jupiter.api.Test
    internal fun testEndSlotRemovable() {
        //TODO addSlot auf Client aufrufen.
        val slotCodeMock = SlotCode("TEST")
        var slotInformationReceiverCapturer = CapturingSlot<SlotInformationReceiver>()
        var eventCapturer = CapturingSlot<Event>()
        var toSendCapturer = CapturingSlot<JSONSlotCodeWrapper>()
        every { clientManagerMock.registerReceiver(slotCodeMock, capture(slotInformationReceiverCapturer)) } returns true

        verify { socketFacadeMock.send(capture(eventCapturer), capture(toSendCapturer)) }
        verify { clientManagerMock.removeReceiver(slotInformationReceiverCapturer.captured) }
        testObj.endSlot(slotCodeMock)
        assertEquals(Event.SLOT_ENDED, eventCapturer.captured)
        assertEquals("TEST", toSendCapturer.captured.getSlotCode().code)

    }



    @org.junit.jupiter.api.Test
    internal fun testEndSlotNotRemovable() {
        val slotCodeMock = SlotCode("TEST")
        var eventCapturer = CapturingSlot<Event>()
        var toSendCapturer = CapturingSlot<JSONSlotCodeWrapper>()
        verify { socketFacadeMock.send(capture(eventCapturer), capture(toSendCapturer)) }
        testObj.endSlot(slotCodeMock)
        assertEquals(Event.INVALID_CODE, eventCapturer.captured)
        assertEquals("TEST", toSendCapturer.captured.getSlotCode().code)
    }

    @org.junit.jupiter.api.Test
    internal fun testDeleteSlotRemovable() {
        //TODO addSlot auf Client aufrufen.
        val slotCodeMock = SlotCode("TEST")
        var slotInformationReceiverCapturer = CapturingSlot<SlotInformationReceiver>()
        var eventCapturer = CapturingSlot<Event>()
        var toSendCapturer = CapturingSlot<JSONSlotCodeWrapper>()
        every { clientManagerMock.registerReceiver(slotCodeMock, capture(slotInformationReceiverCapturer)) } returns true

        verify { socketFacadeMock.send(capture(eventCapturer), capture(toSendCapturer)) }
        verify { clientManagerMock.removeReceiver(slotInformationReceiverCapturer.captured) }
        testObj.deleteSlot(slotCodeMock)
        assertEquals(Event.SLOT_DELETED, eventCapturer.captured)
        assertEquals("TEST", toSendCapturer.captured.getSlotCode().code)

    }



    @org.junit.jupiter.api.Test
    internal fun testDeleteSlotNotRemovable() {
        val slotCodeMock = SlotCode("TEST")
        var eventCapturer = CapturingSlot<Event>()
        var toSendCapturer = CapturingSlot<JSONSlotCodeWrapper>()
        verify { socketFacadeMock.send(capture(eventCapturer), capture(toSendCapturer)) }
        testObj.deleteSlot(slotCodeMock)
        assertEquals(Event.INVALID_CODE, eventCapturer.captured)
        assertEquals("TEST", toSendCapturer.captured.getSlotCode().code)
    }

    @org.junit.jupiter.api.Test
    internal fun testSendSlotData() {
        val slotCodeMock = SlotCode("TEST")
        val slotManagementInformationMock = SlotManagementInformation(ManagementDetails("test", "test@test"),
        Duration.ofMillis(1234), Duration.ofMillis(1234))
        val slotApproxMock = SlotCode("TEST")
        var eventCapturer = CapturingSlot<Event>()
        var toSendCapturer = CapturingSlot<JSONSlotDataWrapper>()
        verify { socketFacadeMock.send(capture(eventCapturer), capture(toSendCapturer)) }
        testObj.sendSlotData(slotCodeMock, Date(1234), slotManagementInformationMock)
        assertEquals(Event.INVALID_CODE, eventCapturer.captured)
        assertEquals("TEST", toSendCapturer.captured.getSlotCode().code)
        assertEquals(1234, toSendCapturer.captured.getSlotApprox().time)
        assertEquals("test", toSendCapturer.captured.getInformation().details.name)
        assertEquals("test@test", toSendCapturer.captured.getInformation().details.email)
        assertEquals(Duration.ofMillis(1234), toSendCapturer.captured.getInformation().delayNotificationTime)
        assertEquals(Duration.ofMillis(1234), toSendCapturer.captured.getInformation().notificationTime)
    }
}
