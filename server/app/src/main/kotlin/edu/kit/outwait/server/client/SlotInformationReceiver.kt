package edu.kit.outwait.server.client

import java.time.Duration
import java.util.Date

import edu.kit.outwait.server.slot.SlotCode
import edu.kit.outwait.server.management.SlotManagementInformation
import edu.kit.outwait.server.management.ManagementDetails

class SlotInformationReceiver(val client: Client, val slotCode: SlotCode) {
    private val slotApprox = Date(0)
    private val slotManagementInformation = SlotManagementInformation(ManagementDetails(""), Duration.ZERO, Duration.ZERO)

    init {

    }

    fun setSlotApprox(slotApprox: Date) {

    }

    fun getSlotApprox(): Date {
        return Date(0)
    }

    fun setSlotManagementInformation(slotManagementInformation: SlotManagementInformation) {

    }

    fun getSlotManagementInformation() {

    }

    fun end() {

    }

    fun delete() {

    }

}
