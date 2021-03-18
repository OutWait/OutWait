package edu.kit.outwait.server.client

import edu.kit.outwait.server.core.Logger
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
    private var slotApprox = Date(0)
    private var slotManagementInformation =
        SlotManagementInformation(ManagementDetails("", ""), Duration.ZERO, Duration.ZERO)
    private val LOG_ID = "SLOT-INFO-RECV"

    /**
     * Called by UpdateMediator on change of Slot data
     *
     * @param slotApprox new Slot ETA
     * @param slotManagementInformation new SlotManagementInformation
     */
    fun setSlotData(slotApprox: Date, slotManagementInformation: SlotManagementInformation) {
        if (this.slotApprox != slotApprox ||
            this.slotManagementInformation != slotManagementInformation
        ) {
            this.slotApprox = slotApprox
            this.slotManagementInformation = slotManagementInformation
            client.sendSlotData(slotCode, slotApprox, slotManagementInformation)
            Logger.debug(LOG_ID, "Updated slot information")
        } else {
            Logger.debug(LOG_ID, "Slot information was not updated (has no changes)")
        }
    }

    /** Getter for Slot ETA f. ex. for "REFRESH_SLOT_APPROX"-Event */
    fun getSlotApprox(): Date {
        return this.slotApprox
    }

    fun getSlotManagementInformation() : SlotManagementInformation {
        return this.slotManagementInformation
    }

    /** Called by UpdateMediator if Slot has been ended by management */
    fun end() {
        Logger.debug(LOG_ID, "Sending slot end")
        client.endSlot(slotCode)
    }

    /** Called by UpdateMediator if Slot has been deleted by management */
    fun delete() {
        Logger.debug(LOG_ID, "Sending slot deletion")
        client.deleteSlot(slotCode)
    }
}
