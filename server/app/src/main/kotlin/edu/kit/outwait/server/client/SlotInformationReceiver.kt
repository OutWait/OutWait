package edu.kit.outwait.server.client

import edu.kit.outwait.server.management.ManagementDetails
import edu.kit.outwait.server.management.SlotManagementInformation
import edu.kit.outwait.server.slot.SlotCode
import java.time.Duration
import java.util.Date

/**
 * Observes UpdateMediator by being registered through DatabaseWrapper. Informed by UpdateMediator
 * on changes
 *
 * @param client Client-Object which has to be informed on changes
 * @param slotCode Slot which is being observed through UpdateMediator
 * @property slotManagementInformation SlotManagementInformation of observed Slot
 * @property slotApprox ETA of observed Slot
 */
class SlotInformationReceiver(val client: Client, val slotCode: SlotCode) {
    private var slotApprox = Date (0)
    private var slotManagementInformation =
        SlotManagementInformation(ManagementDetails(""), Duration.ZERO, Duration.ZERO)

    /**
     * Called by UpdateMediator on change of Slot ETA TODO: Überprüfung auf Änderung Ja/Nein?
     *
     * @param slotApprox new Slot ETA
     */
    fun setSlotApprox(slotApprox: Date) {
        if (this.slotApprox != slotApprox) {
            this.slotApprox = slotApprox
            client.sendSlotApprox(slotCode, this.slotApprox)
        }
    }

    /** Getter for Slot ETA f. ex. for "REFRESH_SLOT_APPROX"-Event */
    fun getSlotApprox(): Date {
        return this.slotApprox
    }

    /**
     * Called by UpdateMediator on change of SlotManagementInformation TODO: Überprüfung auf
     * Änderung Ja/Nein?
     *
     * @param slotManagementInformation new SlotManagementInformation
     */
    fun setSlotManagementInformation(slotManagementInformation: SlotManagementInformation) {
        this.slotManagementInformation = slotManagementInformation
        this.client.sendManagementInformation(slotCode, slotManagementInformation)
    }

    fun getSlotManagementInformation() : SlotManagementInformation {
        return this.slotManagementInformation
    }

    /** Called by UpdateMediator if Slot has been ended by management */
    fun end() {
        client.endSlot(slotCode)
    }

    /** Called by UpdateMediator if Slot has been deleted by management */
    fun delete() {
        client.deleteSlot(slotCode)
    }
}
