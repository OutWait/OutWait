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
        configureReceivers(databaseWrapper)

        // Send settings
        val tmpInfo = databaseWrapper.getManagementById(managementId)!!
        // passed managementId must exist
        managementInformation = tmpInfo
        sendUpdatedManagementSettings(managementInformation.settings)

        // Send queue
        val queueId = databaseWrapper.getQueueIdOfManagement(managementId)
        if (queueId == null) {
            println("INTERNAL ERROR: management has no Queue!")
            // Don't crash the server by a exception. This is just a log.
        } else {
            val queue = Queue(queueId, databaseWrapper)
            sendUpdatedQueue(queue)
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
                databaseWrapper.addTemporarySlot(slot, queue!!.queueId)
                queue!!.addSpontaneousSlot(slot)
                updateAndSendQueue()
            }
        }
        socketFacade.onReceive(Event.ADD_FIXED_SLOT) { json ->
            if (checkTransactionStarted()) {
                val wrapper = json as JSONAddFixedSlotWrapper
                val slot =
                    Slot(
                        SlotCode(""), // will be set by the database
                        Priority.NORMAL,
                        wrapper.getAppointmentTime(),
                        // The creation time is the expected time for new slots
                        wrapper.getDuration(),
                        wrapper.getAppointmentTime()
                    )
                databaseWrapper.addTemporarySlot(slot, queue!!.queueId)
                queue!!.addFixedSlot(slot)
                updateAndSendQueue()
            }
        }
        socketFacade.onReceive(Event.CHANGE_SLOT_DURATION) { json ->
            if (checkTransactionStarted()) {
                val wrapper = json as JSONChangeSlotDurationWrapper
                queue!!.updateSlotLength(wrapper.getSlotCode(), wrapper.getNewDuration())
                updateAndSendQueue()
            }
        }
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

    private fun updateAndSendQueue() {
        if (queue != null) {
            queue!!.updateQueue(managementInformation.settings.prioritizationTime)
            sendUpdatedQueue(queue!!)
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
            if (original_queue != null) {
                sendUpdatedQueue(original_queue)
            }
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
