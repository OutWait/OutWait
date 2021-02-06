package edu.kit.outwait.server.management

import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.protocol.Event
import edu.kit.outwait.server.protocol.JSONAddFixedSlotWrapper
import edu.kit.outwait.server.protocol.JSONAddSpontaneousSlotWrapper
import edu.kit.outwait.server.protocol.JSONChangeSlotDurationWrapper
import edu.kit.outwait.server.protocol.JSONChangeSlotTimeWrapper
import edu.kit.outwait.server.protocol.JSONEmptyWrapper
import edu.kit.outwait.server.protocol.JSONInvalidRequestMessageWrapper
import edu.kit.outwait.server.protocol.JSONManagementSettingsWrapper
import edu.kit.outwait.server.protocol.JSONQueueWrapper
import edu.kit.outwait.server.protocol.JSONSlotCodeWrapper
import edu.kit.outwait.server.protocol.JSONSlotMovementWrapper
import edu.kit.outwait.server.slot.Priority
import edu.kit.outwait.server.slot.Slot
import edu.kit.outwait.server.slot.SlotCode
import edu.kit.outwait.server.socketHelper.SocketFacade
import java.util.Date

class Management(
    private val socketFacade: SocketFacade,
    internal val managementId: ManagementId,
    databaseWrapper:DatabaseWrapper,
    private val managementManager: ManagementManager
) {
    private val managementInformation: ManagementInformation
    private var queue: Queue? = null

    init {
        // Configure the event callbacks
        socketFacade.onReceive(Event.MANAGEMENT_LOGOUT) { logout() }
        socketFacade.onReceive(Event.START_TRANSACTION) { beginNewTransaction() }
        socketFacade.onReceive(Event.ABORT_TRANSACTION) { abortCurrentTransaction() }
        socketFacade.onReceive(Event.SAVE_TRANSACTION) { saveCurrentTransaction() }
        socketFacade.onReceive(Event.DELETE_SLOT) { json ->
            if (checkTransactionStarted()) {
                queue?.deleteSlot((json as JSONSlotCodeWrapper).getSlotCode())
            }
        }
        socketFacade.onReceive(Event.END_CURRENT_SLOT) {
            if (checkTransactionStarted()) {
                queue?.endCurrentSlot()
            }
        }
        socketFacade.onReceive(Event.CHANGE_MANAGEMENT_SETTINGS) { json ->
            changeManagementSettings((json as JSONManagementSettingsWrapper).getSettings())
        }
        socketFacade.onReceive(Event.MOVE_SLOT_AFTER_ANOTHER) { json ->
            if (checkTransactionStarted()) {
                val wrapper = json as JSONSlotMovementWrapper
                queue?.moveSlotAfterAnother(wrapper.getMovedSlot(), wrapper.getOtherSlot())
            }
        }
        socketFacade.onReceive(Event.CHANGE_FIXED_SLOT_TIME) { json ->
            if (checkTransactionStarted()) {
                val wrapper = json as JSONChangeSlotTimeWrapper
                queue?.changeAppointmentTime(wrapper.getSlotCode(), wrapper.getNewTime())
            }
        }
        socketFacade.onReceive(Event.ADD_SPONTANEOUS_SLOT) { json ->
            if (checkTransactionStarted()) {
                val wrapper = json as JSONAddSpontaneousSlotWrapper
                // TODO fix the slot code creation...
                val slot =
                    Slot(
                        SlotCode(""),
                        Priority.NORMAL,
                        Date(),
                        wrapper.getDuration(),
                        wrapper.getCreationTime()
                    )
                queue?.addSpontaneousSlot(slot)
            }
        }
        socketFacade.onReceive(Event.ADD_FIXED_SLOT) { json ->
            if (checkTransactionStarted()) {
                val wrapper = json as JSONAddFixedSlotWrapper
                // TODO fix the slot code creation...
                val slot =
                    Slot(
                        SlotCode(""),
                        Priority.NORMAL,
                        Date(),
                        wrapper.getDuration(),
                        wrapper.getAppointmentTime()
                    )
                queue?.addFixedSlot(slot)
            }
        }
        socketFacade.onReceive(Event.CHANGE_SLOT_DURATION) { json ->
            if (checkTransactionStarted()) {
                val wrapper = json as JSONChangeSlotDurationWrapper
                queue?.updateSlotLength(wrapper.getSlotCode(), wrapper.getNewDuration())
            }
        }

        // Send settings
        managementInformation = databaseWrapper.getManagementById(managementId)
        sendUpdatedManagementSettings(managementInformation.settings)

        // Send queue
        val queueId = databaseWrapper.getQueueIdOfManagement(managementId)
        val queue = Queue(managementId, queueId, databaseWrapper)
        sendUpdatedQueue(queue)
    }

    /**
     * Checks if a transaction has been started. An error message will be send otherwise.
     *
     * @return if the transaction has been started
     */
    private fun checkTransactionStarted() :Boolean {
        if (queue == null) {
            // Transaction has not been started
            val json = JSONInvalidRequestMessageWrapper();
            json.setMessage("Transaction not started")
            socketFacade.send(Event.INVALID_MANAGEMENT_REQUEST, json)
            return false
        } else {
            return true
        }
    }

    internal fun sendUpdatedQueue (queue: Queue):Unit {
        val json = JSONQueueWrapper();
        json.setQueue(queue)
        socketFacade.send(Event.UPDATE_QUEUE, json)
    }
    internal fun sendUpdatedManagementSettings (managementSettings: ManagementSettings) {
        val json = JSONManagementSettingsWrapper();
        json.setSettings(managementSettings)
        socketFacade.send(Event.UPDATE_MANAGEMENT_SETTINGS, json)
    }
    private fun logout () {
        socketFacade.disconnect()
        managementManager.removeManagement(this)
    }
    private fun beginNewTransaction () {
        if (queue != null) {
            // Cannot start a new transaction, when a transaction is running
            val json = JSONInvalidRequestMessageWrapper();
            json.setMessage("Transaction already running")
            socketFacade.send(Event.INVALID_MANAGEMENT_REQUEST, json)
        } else {
            queue = managementManager.beginTransaction(managementId)
            if (queue == null) {
                socketFacade.send(Event.TRANSACTION_DENIED, JSONEmptyWrapper())
            } else {
                socketFacade.send(Event.TRANSACTION_STARTED, JSONEmptyWrapper())
            }
        }
    }
    private fun abortCurrentTransaction () {
        if (checkTransactionStarted()) {
            val original_queue = managementManager.abortTransaction(managementId)
            sendUpdatedQueue(original_queue)
            queue = null // transaction ended
        }
    }
    private fun saveCurrentTransaction () {
        if (checkTransactionStarted()) {
            managementManager.saveTransaction(managementId, queue!!)
            queue = null // transaction ended
        }
    }
    private fun changeManagementSettings (managementSettings: ManagementSettings) {
        managementManager.updateManagementSettings(managementId, managementSettings)
    }
}
