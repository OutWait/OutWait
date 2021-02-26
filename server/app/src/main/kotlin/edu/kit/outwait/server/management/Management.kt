package edu.kit.outwait.server.management

import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.core.Logger
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

class Management(
    private val socketFacade: SocketFacade,
    internal val managementId: ManagementId,
    databaseWrapper:DatabaseWrapper,
    private val managementManager: ManagementManager
) {
    private val managementInformation: ManagementInformation
    private var queue: Queue? = null
    private val LOG_ID = "MGMT"

    init {
        // Configure the event callbacks
        configureReceivers(databaseWrapper)

        Logger.debug(LOG_ID, "New management created and receivers registered")

        // Send settings
        val tmpInfo = databaseWrapper.getManagementById(managementId)!!
        // passed managementId must exist
        managementInformation = tmpInfo
        sendUpdatedManagementSettings(managementInformation.settings)

        Logger.debug(LOG_ID, "Sent settings")

        // Send queue
        val queueId = databaseWrapper.getQueueIdOfManagement(managementId)
        if (queueId == null) {
            Logger.internalError(LOG_ID, "management has no Queue!")
            // Don't crash the server by a exception. This is just a log.
        } else {
            val queue = Queue(queueId, databaseWrapper)
            sendUpdatedQueue(queue)
            Logger.debug(LOG_ID, "Sent first queue")
        }
    }

    private fun configureReceivers(databaseWrapper: DatabaseWrapper) {
        socketFacade.onReceive(Event.MANAGEMENT_LOGOUT) { logout() }
        socketFacade.onReceive(Event.START_TRANSACTION) { beginNewTransaction() }
        socketFacade.onReceive(Event.ABORT_TRANSACTION) { abortCurrentTransaction() }
        socketFacade.onReceive(Event.SAVE_TRANSACTION) { saveCurrentTransaction() }
        socketFacade.onReceive(Event.DELETE_SLOT) { json ->
            if (checkTransactionStarted()) {
                queue!!.deleteSlot((json as JSONSlotCodeWrapper).getSlotCode())
                updateAndSendQueue()
            }
        }
        socketFacade.onReceive(Event.END_CURRENT_SLOT) {
            if (checkTransactionStarted()) {
                queue!!.endCurrentSlot()
                updateAndSendQueue()
            }
        }
        socketFacade.onReceive(Event.CHANGE_MANAGEMENT_SETTINGS) { json ->
            changeManagementSettings((json as JSONManagementSettingsWrapper).getSettings())
        }
        socketFacade.onReceive(Event.MOVE_SLOT_AFTER_ANOTHER) { json ->
            if (checkTransactionStarted()) {
                val wrapper = json as JSONSlotMovementWrapper
                queue!!.moveSlotAfterAnother(wrapper.getMovedSlot(), wrapper.getOtherSlot())
                updateAndSendQueue()
            }
        }
        socketFacade.onReceive(Event.CHANGE_FIXED_SLOT_TIME) { json ->
            if (checkTransactionStarted()) {
                val wrapper = json as JSONChangeSlotTimeWrapper
                queue!!.changeAppointmentTime(wrapper.getSlotCode(), wrapper.getNewTime())
                updateAndSendQueue()
            }
        }
        socketFacade.onReceive(Event.ADD_SPONTANEOUS_SLOT) { json ->
            if (checkTransactionStarted()) {
                val wrapper = json as JSONAddSpontaneousSlotWrapper
                val slot =
                    Slot(
                        SlotCode(""), // will be set by the database
                        Priority.NORMAL,
                        wrapper.getCreationTime(),
                        // The creation time is the expected time for new slots
                        wrapper.getDuration(),
                        wrapper.getCreationTime()
                    )
                val newSlot = databaseWrapper.addTemporarySlot(slot, queue!!.queueId)
                if (newSlot == null) {
                    Logger.internalError(LOG_ID, "failed to create new slot!")
                } else {
                    queue!!.addSpontaneousSlot(newSlot)
                    updateAndSendQueue()
                }
            }
        }
        socketFacade.onReceive(Event.ADD_FIXED_SLOT) { json ->
            if (checkTransactionStarted()) {
                val wrapper = json as JSONAddFixedSlotWrapper
                val slot =
                    Slot(
                        SlotCode(""), // will be set by the database
                        Priority.FIX_APPOINTMENT,
                        wrapper.getAppointmentTime(),
                        // The creation time is the expected time for new slots
                        wrapper.getDuration(),
                        wrapper.getAppointmentTime()
                    )
                val newSlot = databaseWrapper.addTemporarySlot(slot, queue!!.queueId)
                if (newSlot == null) {
                    Logger.internalError(LOG_ID, "failed to create new slot!")
                } else {
                    queue!!.addFixedSlot(newSlot)
                    updateAndSendQueue()
                }
            }
        }
        socketFacade.onReceive(Event.CHANGE_SLOT_DURATION) { json ->
            if (checkTransactionStarted()) {
                val wrapper = json as JSONChangeSlotDurationWrapper
                queue!!.updateSlotLength(wrapper.getSlotCode(), wrapper.getNewDuration())
                updateAndSendQueue()
            }
        }

        socketFacade.onDisconnect {
            logout()
            Logger.debug(LOG_ID, "Implicit logout")
        }

        Logger.debug(LOG_ID, "Receivers configured")
    }

    /**
     * Checks if a transaction has been started. An error message will be send otherwise.
     *
     * @return if the transaction has been started
     */
    private fun checkTransactionStarted() :Boolean {
        if (queue == null) {
            Logger.debug(LOG_ID, "Transaction not started! Can't execute command.")
            // Transaction has not been started
            val json = JSONInvalidRequestMessageWrapper()
            json.setMessage("Transaction not started")
            socketFacade.send(Event.INVALID_MANAGEMENT_REQUEST, json)
            return false
        } else {
            return true
        }
    }

    internal fun isTransactionRunning(): Boolean = queue != null

    private fun updateAndSendQueue() {
        if (queue != null) {
            queue!!.updateQueue(managementInformation.settings.prioritizationTime)
            Logger.debug(LOG_ID, "Queue updated. New queue: " + queue)
            sendUpdatedQueue(queue!!)
        } else {
            Logger.debug(LOG_ID, "Failed to update queue (no queue loaded)")
        }
    }
    internal fun sendUpdatedQueue (queue: Queue):Unit {
        val json = JSONQueueWrapper()
        json.setQueue(queue)
        socketFacade.send(Event.UPDATE_QUEUE, json)
        Logger.debug(LOG_ID, "Sent queue")
    }
    internal fun sendUpdatedManagementSettings (managementSettings: ManagementSettings) {
        val json = JSONManagementSettingsWrapper()
        json.setSettings(managementSettings)
        socketFacade.send(Event.UPDATE_MANAGEMENT_SETTINGS, json)
        Logger.debug(LOG_ID, "Sent management settings")
    }
    private fun logout () {
        socketFacade.disconnect()
        managementManager.removeManagement(this)
        Logger.debug(LOG_ID, "Manual logout completed")
    }
    private fun beginNewTransaction () {
        if (queue != null) {
            Logger.debug(LOG_ID, "New transaction could not be started. Loaded queue: " + queue)
            // Cannot start a new transaction, when a transaction is running
            val json = JSONInvalidRequestMessageWrapper()
            json.setMessage("Transaction already running")
            socketFacade.send(Event.INVALID_MANAGEMENT_REQUEST, json)
        } else {
            Logger.debug(LOG_ID, "Beginning a new transaction")
            queue = managementManager.beginTransaction(managementId)
            if (queue == null) {
                socketFacade.send(Event.TRANSACTION_DENIED, JSONEmptyWrapper())
            } else {
                socketFacade.send(Event.TRANSACTION_STARTED, JSONEmptyWrapper())
            }
        }
    }
    internal fun abortCurrentTransaction () {
        if (checkTransactionStarted()) {
            Logger.debug(LOG_ID, "Aborting transaction")
            val original_queue = managementManager.abortTransaction(managementId)
            if (original_queue != null) {
                sendUpdatedQueue(original_queue)
            }
            queue = null // transaction ended
        } else {
            Logger.debug(LOG_ID, "Could not abort transaction (none running)")
        }
    }
    private fun saveCurrentTransaction () {
        if (checkTransactionStarted()) {
            Logger.debug(LOG_ID, "Saving transaction. Queue: " + queue)
            managementManager.saveTransaction(managementId, queue!!)
            queue = null // transaction ended
        } else {
            Logger.debug(LOG_ID, "Could not save a transaction (none running)")
        }
    }
    private fun changeManagementSettings (managementSettings: ManagementSettings) {
        managementManager.updateManagementSettings(managementId, managementSettings)
        Logger.debug(LOG_ID, "Changed management settings")
    }
}
