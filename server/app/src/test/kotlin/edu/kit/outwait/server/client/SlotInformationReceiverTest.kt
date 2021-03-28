package edu.kit.outwait.server.client

import edu.kit.outwait.server.management.ManagementDetails
import edu.kit.outwait.server.management.SlotManagementInformation
import edu.kit.outwait.server.slot.SlotCode
import io.mockk.*
import java.time.Duration
import java.util.*
import kotlin.test.assertEquals

/** Unit-Tests for SlotInformationReceiver */
class SlotInformationReceiverTest {
    var clientMock = mockk<Client>(relaxed = true)
    var slotCodeMock = mockk<SlotCode>(relaxed = true)
    var testObj = SlotInformationReceiver(clientMock, slotCodeMock)

    /**
     * Creates mock-objects for Client and SlotCde with relaxed mode on since they're needed to
     * instantiate new SlotInformationReceiver object
     */
    @org.junit.jupiter.api.BeforeEach
    fun setUp() {
        clientMock = mockk<Client>(relaxed = true)
        slotCodeMock = mockk<SlotCode>(relaxed = true)
        testObj = SlotInformationReceiver(clientMock, slotCodeMock)
    }

    /** Checks if sendSlotData is called with correct parameters on client */
    @org.junit.jupiter.api.Test
    fun testSetSlotDataChanged() {
        val slotManagementInformationMock =
            SlotManagementInformation(
                ManagementDetails("test", "test@test"),
                Duration.ZERO,
                Duration.ZERO
            )
        val slotApproxMock = Date(1234)
        val slotCodeCapturer = CapturingSlot<SlotCode>()
        val slotApproxCapturer = CapturingSlot<Date>()
        val slotManagementInformationCapturer = CapturingSlot<SlotManagementInformation>()
        every {
            clientMock.sendSlotData(
                capture(slotCodeCapturer),
                capture(slotApproxCapturer),
                capture(slotManagementInformationCapturer)
            )
        } just runs
        testObj.setSlotData(slotApproxMock, slotManagementInformationMock)
        verify {
            clientMock.sendSlotData(slotCodeMock, slotApproxMock, slotManagementInformationMock)
        }
        assertEquals(slotApproxCapturer.captured, slotApproxMock)
        assertEquals(slotCodeCapturer.captured, slotCodeMock)
        assertEquals(slotManagementInformationCapturer.captured, slotManagementInformationMock)
    }

    /** Checks if sendSlotData is NOT called with correct parameters on client */
    @org.junit.jupiter.api.Test
    fun testSetSlotDataNotChanged() {
        val slotManagementInformation =
            SlotManagementInformation(ManagementDetails("", ""), Duration.ZERO, Duration.ZERO)
        val slotApprox = Date(0)

        testObj.setSlotData(slotApprox, slotManagementInformation)
        verify(exactly = 0) { clientMock.sendSlotData(any(), any(), any()) }
    }

    /** Checks if endSlot is called with correct parameters on client */
    @org.junit.jupiter.api.Test
    fun testEndSlot() {
        testObj.end()
        verify { clientMock.endSlot(slotCodeMock) }
    }

    /** Checks if deleteSlot is called with correct parameters on client */
    @org.junit.jupiter.api.Test
    fun testDeleteSlot() {
        testObj.delete()
        verify { clientMock.deleteSlot(slotCodeMock) }
    }
}
