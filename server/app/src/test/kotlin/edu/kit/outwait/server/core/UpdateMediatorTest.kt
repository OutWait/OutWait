package edu.kit.outwait.server.core

import edu.kit.outwait.server.client.SlotInformationReceiver
import edu.kit.outwait.server.management.SlotManagementInformation
import edu.kit.outwait.server.slot.SlotCode
import io.mockk.*
import java.util.*

class UpdateMediatorTest {
    var testObj = UpdateMediator()

    @org.junit.jupiter.api.BeforeEach
    fun setUp() {
        testObj = UpdateMediator()
    }

    /**
     * First tests subscribeReceiver() method. Checks if setSlotData is called with correct
     * parameters Then tests unsubscribeReceiver() method. Checks if setSlotData does NOT call
     * setSlotData on slotInformationReceiverMock
     */
    @org.junit.jupiter.api.Test
    fun testSubscribeAndUnsubscribeReceiver() {
        testObj = spyk<UpdateMediator>()
        val slotInformationReceiverMock = mockk<SlotInformationReceiver>(relaxed = true)
        val slotCodeMock = SlotCode("123")
        val slotApproxMock = Date(123000)
        val slotManagementInformationMock = mockk<SlotManagementInformation>(relaxed = true)

        val slotCodeCapturer = CapturingSlot<SlotCode>()
        val slotApproxCapturer = CapturingSlot<Date>()
        val slotManagementInformationCapturer = CapturingSlot<SlotManagementInformation>()

        //every { testObj getProperty "receivers"} propertyType  answers
        every {
            testObj.setSlotData(
                capture(slotCodeCapturer),
                capture(slotApproxCapturer),
                capture(slotManagementInformationCapturer)
            )
        } just runs
        testObj.subscribeReceiver(
            slotInformationReceiverMock,
            slotCodeMock,
            slotApproxMock,
            slotManagementInformationMock
        )
        verify { testObj.setSlotData(slotCodeMock, slotApproxMock, slotManagementInformationMock) }
        testObj.unsubscribeSlotInformationReceiver(slotInformationReceiverMock)
        testObj.setSlotData(slotCodeMock, slotApproxMock, slotManagementInformationMock)
        verify (exactly = 0) { slotInformationReceiverMock.setSlotData(any(), any()) }
    }

    /** Checks if setSlotData is called with correct parameters on slotInformationReceiverMock */
    @org.junit.jupiter.api.Test
    fun testSetSlotData() {
        val slotInformationReceiverMock = mockk<SlotInformationReceiver>(relaxed = true)
        val slotCodeMock = SlotCode("123")
        val slotApproxMock = Date(123000)
        val slotManagementInformationMock = mockk<SlotManagementInformation>(relaxed = true)
        testObj.subscribeReceiver(
            slotInformationReceiverMock,
            slotCodeMock,
            slotApproxMock,
            slotManagementInformationMock
        )
        testObj.setSlotData(slotCodeMock, Date(321000), slotManagementInformationMock)
        verify {
            slotInformationReceiverMock.setSlotData(Date(321000), slotManagementInformationMock)
        }
    }

    /** Checks if setSlotData is called with correct parameters on slotInformationReceiverMock */
    @org.junit.jupiter.api.Test
    fun testSetSlotApprox() {
        val slotInformationReceiverMock = mockk<SlotInformationReceiver>(relaxed = true)
        val slotCodeMock = SlotCode("123")
        val slotApproxMock = Date(123000)
        val slotManagementInformationMock = mockk<SlotManagementInformation>(relaxed = true)
        testObj.subscribeReceiver(
            slotInformationReceiverMock,
            slotCodeMock,
            slotApproxMock,
            slotManagementInformationMock
        )
        testObj.setSlotApprox(slotCodeMock, Date(321000))
        verify {
            slotInformationReceiverMock.setSlotData(
                Date(321000),
                slotInformationReceiverMock.getSlotManagementInformation()
            )
        }
    }

    /** Checks if setSlotData is called with correct parameters on slotInformationReceiverMock */
    @org.junit.jupiter.api.Test
    fun testSetManagementInformation() {
        val slotInformationReceiverMock = mockk<SlotInformationReceiver>(relaxed = true)
        val slotCodeMock = SlotCode("123")
        val slotApproxMock = Date(123000)
        val slotManagementInformationMock = mockk<SlotManagementInformation>(relaxed = true)
        testObj.subscribeReceiver(
            slotInformationReceiverMock,
            slotCodeMock,
            slotApproxMock,
            slotManagementInformationMock
        )
        val newSlotManagementInformationMock = mockk<SlotManagementInformation>(relaxed = true)
        val slotCodeMocks = mutableListOf<SlotCode>()
        slotCodeMocks.add(slotCodeMock)
        testObj.setManagementInformation(slotCodeMocks, newSlotManagementInformationMock)
        verify { slotInformationReceiverMock.setSlotData(any(), newSlotManagementInformationMock) }
    }

    /** Checks if end is called on slotInformationReceiver of ended slot */
    @org.junit.jupiter.api.Test
    fun testEndSlot() {
        val slotInformationReceiverMock = mockk<SlotInformationReceiver>(relaxed = true)
        val slotCodeMock = SlotCode("123")
        val slotApproxMock = Date(123000)
        val slotManagementInformationMock = mockk<SlotManagementInformation>(relaxed = true)
        testObj.subscribeReceiver(
            slotInformationReceiverMock,
            slotCodeMock,
            slotApproxMock,
            slotManagementInformationMock
        )
        testObj.endSlot(slotCodeMock)
        verify { slotInformationReceiverMock.end() }
    }

    /** Checks if delete is called on slotInformationReceiver of deleted slot */
    @org.junit.jupiter.api.Test
    fun testDeleteSlot() {
        val slotInformationReceiverMock = mockk<SlotInformationReceiver>(relaxed = true)
        val slotCodeMock = SlotCode("123")
        val slotApproxMock = Date(123000)
        val slotManagementInformationMock = mockk<SlotManagementInformation>(relaxed = true)
        testObj.subscribeReceiver(
            slotInformationReceiverMock,
            slotCodeMock,
            slotApproxMock,
            slotManagementInformationMock
        )
        testObj.deleteSlot(slotCodeMock)
        verify { slotInformationReceiverMock.delete() }
    }
}
