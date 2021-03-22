package edu.kit.outwait.server.management

import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.core.InternalServerErrorException
import edu.kit.outwait.server.core.Logger
import edu.kit.outwait.server.protocol.Event
import edu.kit.outwait.server.protocol.JSONAddFixedSlotWrapper
import edu.kit.outwait.server.protocol.JSONAddSpontaneousSlotWrapper
import edu.kit.outwait.server.protocol.JSONChangeSlotDurationWrapper
import edu.kit.outwait.server.protocol.JSONChangeSlotTimeWrapper
import edu.kit.outwait.server.protocol.JSONEmptyWrapper
import edu.kit.outwait.server.protocol.JSONErrorMessageWrapper
import edu.kit.outwait.server.protocol.JSONManagementSettingsWrapper
import edu.kit.outwait.server.protocol.JSONQueueWrapper
import edu.kit.outwait.server.protocol.JSONSlotCodeWrapper
import edu.kit.outwait.server.protocol.JSONSlotMovementWrapper
import edu.kit.outwait.server.slot.Priority
import edu.kit.outwait.server.slot.Slot
import edu.kit.outwait.server.slot.SlotCode
import edu.kit.outwait.server.socketHelper.SocketFacade
import java.time.Duration
import java.util.Date

/**
 * Representation of a connection to a manager.
 *
 * All communication with the management-side user (except the login) is done through this class. It
 * holds the socket and handles all incoming requests, as well as outgoing messages.
 *
 * @property socketFacade the created socket to the manager with a running connection
 * @property managementId the id of the management (loaded from the DB)
 * @param databaseWrapper the DB to complete the initialization of the management
 * @property managementManager the ManagementManager which administrates all managements
 * @constructor initializes the object to receive requests and automatically sends management
 *     settings and the queue to the management
 */
class Management(
    private val socketFacade: SocketFacade,
    internal val managementId: ManagementId,
    databaseWrapper:DatabaseWrapper,
    private val managementManager: ManagementManager
) {
    private val managementInformation: ManagementInformation
    /** The queue of this management. Only loaded on demand, when a transaction is running. */
    private var queue: Queue? = null
    private val LOG_ID = "MGMT"

    init {
        // Configure the event callbacks
        configureReceivers(databaseWrapper)

        Logger.debug(LOG_ID, "New management created and receivers registered")

        // Send settings
        // Passed managementId must exist
        val tmpInfo = databaseWrapper.getManagementById(managementId)!!
        managementInformation = tmpInfo
        sendUpdatedManagementSettings(managementInformation.settings)

        Logger.debug(LOG_ID, "Sent settings")

        // Send queue
        val queueId = databaseWrapper.getQueueIdOfManagement(managementId)
        if (queueId == null) {
            Logger.internalError(LOG_ID, "management has no Queue!")
            sendInternalErrorMessage("Management has no corresponding queue.")
            // Don't crash the server by a exception. This is just a log.
        } else {
            val queue = Queue(queueId, databaseWrapper)
            // Update the queue as it might have been loaded in wrong order.
            queue.updateQueue(managementInformation.settings.prioritizationTime)
            sendUpdatedQueue(queue)
            Logger.debug(LOG_ID, "Sent first queue")
        }
    }

    /**
     * Configures the message receivers.
     *
     * The handlers for all possible (allowed) incoming messages are registered here. Lambdas are
     * used to handle basic logic like unwrapping of json wrappers, but most of the business logic
     * is done in dedicated methods.
     *
     * @param databaseWrapper used for simple operations inside the handlers, like (temporarily)
     *     adding new slots
     */
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
                queue!!.updateQueue(managementInformation.settings.prioritizationTime)
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
                addNewSlot(
                    wrapper.getDuration(),
                    wrapper.getCreationTime(),
                    Priority.NORMAL,
                    databaseWrapper
                )
            }
        }
        socketFacade.onReceive(Event.ADD_FIXED_SLOT) { json ->
            if (checkTransactionStarted()) {
                val wrapper = json as JSONAddFixedSlotWrapper
                addNewSlot(
                    wrapper.getDuration(),
                    wrapper.getAppointmentTime(),
                    Priority.FIX_APPOINTMENT,
                    databaseWrapper
                )
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

    private fun addNewSlot(
        expectedDuration: Duration,
        constructionTime: Date,
        priority: Priority,
        databaseWrapper: DatabaseWrapper
    ) {
        // First check if the constructionTime is valid
        if (constructionTime.toInstant().isBefore(Date().toInstant() - Duration.ofMinutes(1))) {
            val json = JSONErrorMessageWrapper()
            json.setMessage("Tried to create slot in the past")
            socketFacade.send(Event.INVALID_MANAGEMENT_REQUEST, json)
            Logger.debug(
                LOG_ID,
                "Tried to create slot in the past. Server time: " + Date() +
                    ". Tried to create at: " + constructionTime
            )
        } else if (constructionTime.toInstant().isAfter(Date().toInstant() + Duration.ofHours(24))
        ) {
            val json = JSONErrorMessageWrapper()
            json.setMessage("Tried to create slot more than 24h in the future")
            socketFacade.send(Event.INVALID_MANAGEMENT_REQUEST, json)
            Logger.debug(LOG_ID, "Tried to create slot more than 24h in the future")
        } else {
            // Slot time is valid
            val slot =
                Slot(
                    SlotCode(""), // will be set by the database
                    priority,
                    // The creation time is the expected time for new slots
                    constructionTime,
                    expectedDuration,
                    constructionTime
                )
            val newSlot = databaseWrapper.addTemporarySlot(slot, queue!!.queueId)
            if (newSlot == null) {
                Logger.internalError(LOG_ID, "Failed to create new slot!")
                sendInternalErrorMessage("Failed to create slot.")
            } else {
                queue!!.addSlot(newSlot)
                updateAndSendQueue()
            }
        }
    }

    /**
     * Checks if a transaction has been started.
     *
     * An error message will be send to the management, if the transaction has not been started jet.
     *
     * @return whether the transaction has been started.
     */
    private fun checkTransactionStarted() :Boolean {
        if (!isTransactionRunning()) {
            Logger.debug(LOG_ID, "Transaction not started! Can't execute command.")
            // Transaction has not been started
            val json = JSONErrorMessageWrapper()
            json.setMessage("Transaction not started")
            socketFacade.send(Event.INVALID_MANAGEMENT_REQUEST, json)
            return false
        } else {
            return true
        }
    }

    /**
     * Detects whether a transaction is running.
     *
     * @return whether a transaction is running.
     */
    internal fun isTransactionRunning(): Boolean = queue != null

    /**
     * Updates the current queue and sends the new queue state to the management.
     *
     * Calling this requires a transaction to be started.
     */
    private fun updateAndSendQueue() {
        if (isTransactionRunning()) {
            queue!!.updateQueue(managementInformation.settings.prioritizationTime)
            Logger.debug(LOG_ID, "Queue updated. New queue: " + queue)
            sendUpdatedQueue(queue!!)
        } else {
            Logger.error(LOG_ID, "Failed to update queue (no queue loaded)")
        }
    }

    /**
     * Sends the given queue to the management.
     *
     * @param queue the queue which should be send.
     */
    internal fun sendUpdatedQueue (queue: Queue):Unit {
        val json = JSONQueueWrapper()
        json.setQueue(queue)
        //Thread.sleep(1000) // DEBUG
        socketFacade.send(Event.UPDATE_QUEUE, json)
        Logger.debug(LOG_ID, "Sent queue")
    }

    /**
     * Sends the given management settings to the management.
     *
     * @param managementSettings the setting which should be send.
     */
    internal fun sendUpdatedManagementSettings (managementSettings: ManagementSettings) {
        val json = JSONManagementSettingsWrapper()
        json.setSettings(managementSettings)
        socketFacade.send(Event.UPDATE_MANAGEMENT_SETTINGS, json)
        Logger.debug(LOG_ID, "Sent management settings")
    }

    /**
     * Sends an error message to the manager with the given message.
     *
     * @param message the message string to send to the manager.
     */
    internal fun sendInternalErrorMessage(message: String) {
        val json = JSONErrorMessageWrapper()
        json.setMessage(message)
        socketFacade.send(Event.INTERNAL_SERVER_ERROR, json)
        Logger.debug(LOG_ID, "Sent internal error message")
    }

    /**
     * Initiates the logout routine.
     *
     * It will remove the management from the manger and thus abort the running transaction if
     * needed.
     */
    private fun logout () {
        managementManager.removeManagement(this)
        socketFacade.disconnect()
        Logger.debug(LOG_ID, "Manual logout completed")
    }

    /**
     * Starts a new transaction if possible.
     *
     * It will send an error message, if a transaction is already running (for this institution).
     */
    private fun beginNewTransaction () {
        if (isTransactionRunning()) {
            Logger.debug(LOG_ID, "New transaction could not be started. Loaded queue: " + queue)
            // Cannot start a new transaction, when a transaction is running
            val json = JSONErrorMessageWrapper()
            json.setMessage("Transaction already running")
            socketFacade.send(Event.INVALID_MANAGEMENT_REQUEST, json)
        } else {
            Logger.debug(LOG_ID, "Beginning a new transaction")
            try {
                queue = managementManager.beginTransaction(managementId)
                if (!isTransactionRunning()) {
                    socketFacade.send(Event.TRANSACTION_DENIED, JSONEmptyWrapper())
                } else {
                    socketFacade.send(Event.TRANSACTION_STARTED, JSONEmptyWrapper())
                }
            } catch (e: InternalServerErrorException) {
                sendInternalErrorMessage(e.message!!)
            }
        }
    }

    /**
     * Aborts the currently running transaction.
     *
     * It will send an error message, if no transaction is running. Otherwise the old queue is send
     * to the management (the one before the transaction).
     */
    internal fun abortCurrentTransaction () {
        if (checkTransactionStarted()) {
            Logger.debug(LOG_ID, "Aborting transaction")
            try {
                val original_queue = managementManager.abortTransaction(managementId)
                // Update the queue as it might have been loaded in wrong order.
                original_queue.updateQueue(managementInformation.settings.prioritizationTime)
                sendUpdatedQueue(original_queue)
                queue = null // transaction ended
            } catch (e: InternalServerErrorException) {
                sendInternalErrorMessage(e.message!!)
            }
        } else {
            Logger.debug(LOG_ID, "Could not abort transaction (none running)")
        }
    }

    /**
     * Saves the currently running transaction.
     *
     * It will send an error message, if no transaction is running. Otherwise the current queue is
     * send to all managements of this institution and saved in the database.
     */
    private fun saveCurrentTransaction () {
        if (checkTransactionStarted()) {
            Logger.debug(LOG_ID, "Saving transaction. Queue: " + queue)
            try {
                managementManager.saveTransaction(managementId, queue!!)
                queue = null // transaction ended
            } catch (e: InternalServerErrorException) {
                sendInternalErrorMessage(e.message!!)
            }
        } else {
            Logger.debug(LOG_ID, "Could not save a transaction (none running)")
        }
    }

    /**
     * Changes the settings of this management
     *
     * All managements of this institution will receive the new settings and the settings are saved
     * in the database.
     *
     * @param managementSettings the new settings to save
     */
    private fun changeManagementSettings (managementSettings: ManagementSettings) {
        try {
            managementManager.updateManagementSettings(managementId, managementSettings)
            Logger.debug(LOG_ID, "Changed management settings")
        } catch (e: InternalServerErrorException) {
            sendInternalErrorMessage(e.message!!)
        }
    }
}
