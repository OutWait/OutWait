package edu.kit.outwait.server.client

import edu.kit.outwait.server.management.ManagementDetails
import edu.kit.outwait.server.management.SlotManagementInformation
import edu.kit.outwait.server.protocol.*
import edu.kit.outwait.server.slot.SlotCode
import edu.kit.outwait.server.socketHelper.SocketFacade
import io.mockk.*
import java.sql.Date
import java.time.Duration
import kotlin.test.assertEquals

/** Unit-Test for ClientTest */
class ClientTest {
    var socketFacadeMock = mockk<SocketFacade>(relaxed = true)
    var clientManagerMock = mockk<ClientManager>(relaxed = true)
    var testObj = Client(socketFacadeMock, clientManagerMock)

    /**
     * Creates mock-objects for socketFacade and clientManager with relaxed mode on since they're
     * needed to instantiate new ClientManagerObject
     */
    @org.junit.jupiter.api.BeforeEach
    fun setUp() {
        socketFacadeMock = mockk<SocketFacade>(relaxed = true)
        clientManagerMock = mockk<ClientManager>(relaxed = true)
        testObj = Client(socketFacadeMock, clientManagerMock)
    }

    /**
     * Calls listenSlot on Socket so that a Slot with SlotCode is added. So the callback function
     * can be captured. With the captured function we can register a new slot with his register on
     * client. Ensures that registerReceiver on clientManager returns true to be able to remove the
     * receiver in the endSlot method. Checks if removeReceiver is called with correct parameters on
     * clientManager Checks if Event SLOT_ENDED is send with correct SlotCode
     */
    @org.junit.jupiter.api.Test
    fun testEndSlotRemovable() {
        socketFacadeMock = mockk<SocketFacade>(relaxed = true)
        val functionCapturer = CapturingSlot<(receivedData: JSONObjectWrapper) -> Unit>()
        val eventCapturer = CapturingSlot<Event>()
        val toSendCapturer = CapturingSlot<JSONSlotCodeWrapper>()
        val slotCodeMock = SlotCode("TEST")
        val slotInformationReceiverCapturer = CapturingSlot<SlotInformationReceiver>()

        //capture callback
        every { socketFacadeMock.onReceive(Event.LISTEN_SLOT, capture(functionCapturer)) } just runs
        every { socketFacadeMock.send(capture(eventCapturer), capture(toSendCapturer)) } just runs
        every {
            clientManagerMock.registerReceiver(
                slotCodeMock,
                capture(slotInformationReceiverCapturer)
            )
        } returns true

        testObj = Client(socketFacadeMock, clientManagerMock)
        val slotCodeWrapperMock = JSONSlotCodeWrapper()
        slotCodeWrapperMock.setSlotCode(SlotCode("TEST"))
        functionCapturer.captured(slotCodeWrapperMock)

        testObj.endSlot(slotCodeMock)
        verify {
            socketFacadeMock.send(Event.READY_TO_SERVE, any())
            clientManagerMock.removeReceiver(slotInformationReceiverCapturer.captured)
            socketFacadeMock.send(Event.SLOT_ENDED, any())
        }
        assertEquals(Event.SLOT_ENDED, eventCapturer.captured)
        assertEquals("TEST", toSendCapturer.captured.getSlotCode().code)
    }

    /**
     * Checks if send is called with correct parameters on socketFacade for a non removable Slot.
     */
    @org.junit.jupiter.api.Test
    fun testEndSlotNotRemovable() {
        val slotCodeMock = SlotCode("TEST")
        val eventCapturer = CapturingSlot<Event>()
        val toSendCapturer = CapturingSlot<JSONSlotCodeWrapper>()
        every { socketFacadeMock.send(capture(eventCapturer), capture(toSendCapturer)) } just runs
        testObj.endSlot(slotCodeMock)
        verify {
            socketFacadeMock.send(Event.READY_TO_SERVE, any())
            socketFacadeMock.send(Event.INVALID_CODE, toSendCapturer.captured)
        }
        assertEquals(Event.INVALID_CODE, eventCapturer.captured)
        assertEquals("TEST", toSendCapturer.captured.getSlotCode().code)
    }

    /**
     * Calls listenSlot on Socket so that a Slot with SlotCode is added. So the callback function
     * can be captured. With the captured function we can register a new slot with his register on
     * client. Ensures that registerReceiver on clientManager returns true to be able to remove the
     * receiver in the deleteSlot method. Checks if removeReceiver is called with correct parameters
     * on clientManager Checks if Event SLOT_DELETED is send with correct SlotCode
     */
    @org.junit.jupiter.api.Test
    fun testDeleteSlotRemovable() {
        socketFacadeMock = mockk<SocketFacade>(relaxed = true)
        val functionCapturer = CapturingSlot<(receivedData: JSONObjectWrapper) -> Unit>()
        val eventCapturer = CapturingSlot<Event>()
        val toSendCapturer = CapturingSlot<JSONSlotCodeWrapper>()
        val slotCodeMock = SlotCode("TEST")
        val slotInformationReceiverCapturer = CapturingSlot<SlotInformationReceiver>()

        //capture callback
        every { socketFacadeMock.onReceive(Event.LISTEN_SLOT, capture(functionCapturer)) } just runs
        every { socketFacadeMock.send(capture(eventCapturer), capture(toSendCapturer)) } just runs
        every {
            clientManagerMock.registerReceiver(
                slotCodeMock,
                capture(slotInformationReceiverCapturer)
            )
        } returns true

        testObj = Client(socketFacadeMock, clientManagerMock)
        val slotCodeWrapperMock = JSONSlotCodeWrapper()
        slotCodeWrapperMock.setSlotCode(SlotCode("TEST"))
        functionCapturer.captured(slotCodeWrapperMock)

        testObj.deleteSlot(slotCodeMock)
        verify {
            socketFacadeMock.send(Event.READY_TO_SERVE, any())
            clientManagerMock.removeReceiver(slotInformationReceiverCapturer.captured)
            socketFacadeMock.send(Event.SLOT_DELETED, any())
        }
        assertEquals(Event.SLOT_DELETED, eventCapturer.captured)
        assertEquals("TEST", toSendCapturer.captured.getSlotCode().code)
    }

    /**
     * Checks if send is called with correct parameters on socketFacade for a non removable Slot.
     */
    @org.junit.jupiter.api.Test
    fun testDeleteSlotNotRemovable() {
        val slotCodeMock = SlotCode("TEST")
        val slotCodeWrapperMock = JSONSlotCodeWrapper()
        slotCodeWrapperMock.setSlotCode(slotCodeMock)
        val eventCapturer = CapturingSlot<Event>()
        val toSendCapturer = CapturingSlot<JSONSlotCodeWrapper>()
        every { socketFacadeMock.send(capture(eventCapturer), capture(toSendCapturer)) } just runs
        testObj.deleteSlot(slotCodeMock)
        verify {
            socketFacadeMock.send(Event.READY_TO_SERVE, any())
            socketFacadeMock.send(Event.INVALID_CODE, toSendCapturer.captured)
        }
        assertEquals(Event.INVALID_CODE, eventCapturer.captured)
        assertEquals("TEST", toSendCapturer.captured.getSlotCode().code)
    }

    /** Checks if send is called with correct parameters on socketFacade for Slot data. */
    @org.junit.jupiter.api.Test
    fun testSendSlotData() {
        val slotCodeMock = SlotCode("TEST")
        val slotManagementInformationMock =
            SlotManagementInformation(
                ManagementDetails("test", "test@test"),
                Duration.ofMillis(1234),
                Duration.ofMillis(1234)
            )
        val eventCapturer = CapturingSlot<Event>()
        val toSendCapturer = CapturingSlot<JSONSlotDataWrapper>()
        every { socketFacadeMock.send(capture(eventCapturer), capture(toSendCapturer)) } just runs
        testObj.sendSlotData(slotCodeMock, Date(1234), slotManagementInformationMock)
        verify {
            socketFacadeMock.send(Event.READY_TO_SERVE, any())
            socketFacadeMock.send(Event.SEND_SLOT_DATA, any())
        }
        assertEquals(Event.SEND_SLOT_DATA, eventCapturer.captured)
        assertEquals("TEST", toSendCapturer.captured.getSlotCode().code)
        assertEquals(1234, toSendCapturer.captured.getSlotApprox().time)
        assertEquals("test", toSendCapturer.captured.getInformation().details.name)
        assertEquals(
            Duration.ofMillis(1234),
            toSendCapturer.captured.getInformation().delayNotificationTime
        )
        assertEquals(
            Duration.ofMillis(1234),
            toSendCapturer.captured.getInformation().notificationTime
        )
    }
}
