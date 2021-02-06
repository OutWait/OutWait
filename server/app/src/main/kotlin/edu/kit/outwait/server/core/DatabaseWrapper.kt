package edu.kit.outwait.server.core

import edu.kit.outwait.server.client.SlotInformationReceiver
import edu.kit.outwait.server.management.ManagementCredentials
import edu.kit.outwait.server.management.ManagementDetails
import edu.kit.outwait.server.management.ManagementId
import edu.kit.outwait.server.management.ManagementInformation
import edu.kit.outwait.server.management.ManagementSettings
import edu.kit.outwait.server.management.Mode
import edu.kit.outwait.server.slot.Slot
import edu.kit.outwait.server.slot.SlotCode
import java.time.Duration
import java.util.Date

// TODO fix the stub
class DatabaseWrapper {
    //val connection: Connection
    //val properties: Properties
    //val updateMediator: UpdateMediator

    init {
    }

    fun getSlots(managementId: ManagementId): List<Slot> {
        return listOf<Slot>()
    }

    fun getSlotApprox(slotCode: SlotCode): Date {
        return Date(0)
    }

    fun setSloxApprox(slotCode: SlotCode, slotApprox: Date) {
    }

    fun saveSlots(slots: List<Slot>) {
    }

    fun getManagementById(managementId: ManagementId): ManagementInformation {
        return ManagementInformation(
            ManagementDetails(""),
            ManagementSettings(Mode.ONE, Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO)
        )
    }

    fun getManagementByUsername(username: String): ManagementCredentials {
        return ManagementCredentials(ManagementId(0), "", "")
    }

    fun saveManagementSettings(managementSettings: ManagementSettings) {
    }

    fun registerReceiver(receiver: SlotInformationReceiver, slotCode: SlotCode): Boolean {
        return true
    }

    fun unregisterReceiver(receiver: SlotInformationReceiver) {
    }

    fun changeManagementPassword(username: String, password: String) {
    }

    fun endSlot(slotCode: SlotCode) {
    }

    fun deleteSlot(slotCode: SlotCode) {
    }
}
