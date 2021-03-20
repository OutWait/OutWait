package edu.kit.outwait.server.client

import com.corundumstudio.socketio.SocketIONamespace
import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.management.ManagementDetails
import edu.kit.outwait.server.management.SlotManagementInformation
import edu.kit.outwait.server.slot.SlotCode
import io.mockk.*
import java.time.Duration
import java.util.*
import kotlin.test.assertEquals

class SlotInformationReceiverTest {
    var clientMock = mockk<Client>(relaxed = true)
    var slotCodeMock = mockk<SlotCode>(relaxed = true)
    var testObj = SlotInformationReceiver(clientMock, slotCodeMock)

    @org.junit.jupiter.api.BeforeEach
    fun setUp() {
        clientMock = mockk<Client>(relaxed = true)
        slotCodeMock = mockk<SlotCode>(relaxed = true)
        testObj = SlotInformationReceiver(clientMock, slotCodeMock)
    }

    @org.junit.jupiter.api.Test
    internal fun testSetSlotDataChanged() {
        val slotManagementInformationMock = SlotManagementInformation(ManagementDetails("test", "test@test"), Duration.ZERO, Duration.ZERO)
        val slotApproxMock = Date(1234)
        val slotCodeCapturer = CapturingSlot<SlotCode>()
        val slotApproxCapturer = CapturingSlot<Date>()
        val slotManagementInformationCapturer = CapturingSlot<SlotManagementInformation>()
        every { clientMock.sendSlotData(capture(slotCodeCapturer), capture(slotApproxCapturer), capture(slotManagementInformationCapturer))} just runs
        testObj.setSlotData(slotApproxMock, slotManagementInformationMock)
        verify { clientMock.sendSlotData(slotCodeMock, slotApproxMock, slotManagementInformationMock) }
        assertEquals(slotApproxCapturer.captured, slotApproxMock)
        assertEquals(slotCodeCapturer.captured, slotCodeMock)
        assertEquals(slotManagementInformationCapturer.captured, slotManagementInformationMock)

    }

    @org.junit.jupiter.api.Test
    internal fun testSetSlotDataNotChanged() {
        val slotManagementInformation = SlotManagementInformation(ManagementDetails("", ""), Duration.ZERO, Duration.ZERO)
        val slotApprox = Date(0)

        testObj.setSlotData(slotApprox, slotManagementInformation)
        verify(exactly = 0) {
           clientMock.sendSlotData(any(), any(), any())
        }
    }

    @org.junit.jupiter.api.Test
    internal fun testEndSlot() {
        testObj.end()
        verify {
            clientMock.endSlot(slotCodeMock)
        }
    }

    @org.junit.jupiter.api.Test
    internal fun testDeleteSlot() {
        testObj.delete()
        verify {
            clientMock.deleteSlot(slotCodeMock)
        }
    }
}
