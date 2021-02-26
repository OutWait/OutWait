package edu.kit.outwait.server.management

import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIONamespace
import edu.kit.outwait.server.core.AbstractManager
import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.core.Logger
import edu.kit.outwait.server.protocol.Event
import edu.kit.outwait.server.protocol.JSONCredentialsWrapper
import edu.kit.outwait.server.protocol.JSONEmptyWrapper
import edu.kit.outwait.server.protocol.JSONResetPasswordWrapper
import edu.kit.outwait.server.socketHelper.SocketFacade
import java.util.Date
import java.util.Timer

class ManagementManager(namespace: SocketIONamespace, databaseWrapper: DatabaseWrapper) :
    AbstractManager(namespace, databaseWrapper) {

    private val managements = mutableListOf<Management>()
    private val activeTransactions = hashSetOf<ManagementId>()
    private val queueDelayTimes = mutableListOf<Pair<Date, ManagementId>>()
    private val nextDelayAlarm = Timer()
    private val LOG_ID = "MGMT-MGR"

    init {
        val events =
            listOf(
                Event.MANAGEMENT_LOGIN,
                Event.MANAGEMENT_LOGOUT,
                Event.START_TRANSACTION,
                Event.ABORT_TRANSACTION,
                Event.SAVE_TRANSACTION,
                Event.DELETE_SLOT,
                Event.END_CURRENT_SLOT,
                Event.CHANGE_MANAGEMENT_SETTINGS,
                Event.MOVE_SLOT_AFTER_ANOTHER,
                Event.CHANGE_FIXED_SLOT_TIME,
                Event.ADD_SPONTANEOUS_SLOT,
                Event.ADD_FIXED_SLOT,
                Event.CHANGE_SLOT_DURATION,
                Event.RESET_PASSWORD
            )
        socketAdapter.configureEvents(events)
        Logger.debug(LOG_ID, "Management manager initialized")
    }

    override fun bindSocket(socket: SocketIOClient, socketFacade: SocketFacade) {
        // Handle the login
        socketFacade.onReceive(Event.MANAGEMENT_LOGIN) { json ->
            val wrapper = (json as JSONCredentialsWrapper)
            Logger.debug(LOG_ID, "New login of: " + wrapper)
            val credentials = databaseWrapper.getManagementByUsername(wrapper.getUsername())

            if (credentials == null || wrapper.getPassword() != credentials.password) {
                Logger.debug(LOG_ID, "Access denied")
                socketFacade.send(Event.MANAGEMENT_LOGIN_DENIED, JSONEmptyWrapper())
                socketFacade.disconnect()
            } else {
                Logger.debug(LOG_ID, "Access granted. Starting management " + credentials.id)
                socketFacade.send(Event.MANAGEMENT_LOGIN_SUCCESS, JSONEmptyWrapper())

                // Create new management instance
                val manager = Management(socketFacade, credentials.id, databaseWrapper, this)
                managements.add(manager)
            }
        }

        // Handle the reset password function
        socketFacade.onReceive(Event.RESET_PASSWORD) { json ->
            Logger.debug(LOG_ID, "Password resetting routine started")
            resetManagementPassword((json as JSONResetPasswordWrapper).getUsername())
        }

        // Login request
        Logger.debug(LOG_ID, "Starting login routine")
        socketFacade.send(Event.LOGIN_REQUEST, JSONEmptyWrapper())
    }
    fun removeManagement(management: Management) {
        Logger.debug(LOG_ID, "Removing management connection")
        // Close open transactions
        if (management.isTransactionRunning()) management.abortCurrentTransaction()

        managements.remove(management)
    }
    fun beginTransaction(managementId: ManagementId): Queue? {
        if (activeTransactions.contains(managementId)) {
            Logger.debug(
                LOG_ID,
                "New transaction denied. Already running in management " + managementId
            )
            return null
        } else {
            Logger.debug(LOG_ID, "New transaction granted")
            activeTransactions.add(managementId)

            // Load the queue
            val queueId = databaseWrapper.getQueueIdOfManagement(managementId)
            if (queueId == null) {
                Logger.internalError(LOG_ID, "Management has no Queue!")
                // Don't crash the server by a exception. This is just a log.
                return null
            } else {
                Logger.debug(LOG_ID, "New transaction queue loaded")
                return Queue(queueId, databaseWrapper)
            }
        }
    }
    fun abortTransaction(managementId: ManagementId): Queue? {
        Logger.debug(LOG_ID, "Aborting transaction of management " + managementId + "...")
        assert(activeTransactions.contains(managementId))

        activeTransactions.remove(managementId)
        Logger.debug(LOG_ID, "Transaction aborted.")
        Logger.debug(LOG_ID, "Active transaction removed")

        // Re-load the queue with the state before the transaction
        val queueId = databaseWrapper.getQueueIdOfManagement(managementId)
        if (queueId == null) {
            Logger.internalError(LOG_ID, "Management has no Queue!")
            // Don't crash the server by a exception. This is just a log.
            return null
        } else {
            // delete all temporary slots
            Logger.debug(LOG_ID, "Deleting temporary slots...")
            if (!databaseWrapper.deleteAllTemporarySlots(queueId)) return null
            Logger.debug(LOG_ID, "Temporary slots deleted.")
            return Queue(queueId, databaseWrapper)
        }
    }

    /**
     * Is called internally when the queue has been changed somewhere. It will store the queue in
     * the database, inform all Managements and update the next-delay-alarm.
     */
    private fun handleQueueUpdate(managementId: ManagementId, queue: Queue) {
        Logger.debug(
            LOG_ID,
            "Saving updated queue for management " + managementId + ", new queue: " + queue
        )
        queue.storeToDB(databaseWrapper)

        // Distribute the queue
        for (management in managements) {
            if (management.managementId == managementId) {
                management.sendUpdatedQueue(queue)
            }
        }

        // Create delay timer
        Logger.debug(LOG_ID, "Saving queue delay change for later")
        val nextDelayChange = queue.calculateNextDelayChange()
        if (nextDelayChange != null) {
            keepQueueDelayTime(nextDelayChange, managementId)
        }
    }

    fun saveTransaction(managementId: ManagementId, queue: Queue) {
        Logger.debug(LOG_ID, "Saving transaction. Checking...")
        assert(activeTransactions.contains(managementId))
        Logger.debug(LOG_ID, "Check done.")

        handleQueueUpdate(managementId, queue)

        activeTransactions.remove(managementId)
        Logger.debug(LOG_ID, "Active transaction removed")
    }
    fun updateManagementSettings(
        managementId: ManagementId,
        managementSettings: ManagementSettings
    ) {
        Logger.debug(
            LOG_ID,
            "Updating management settings of " + managementId + " with settings: " +
                managementSettings
        )
        databaseWrapper.saveManagementSettings(managementId, managementSettings)

        for (management in managements) {
            if (management.managementId == managementId)
                management.sendUpdatedManagementSettings(managementSettings)
        }
    }
    private fun resetManagementPassword(username: String) {
        Logger.debug(LOG_ID, "Reset password routine started")
        // TODO implement the reset password procedure
    }
    fun keepQueueDelayTime(time: Date, managementId: ManagementId) {
        queueDelayTimes.add(Pair(time, managementId))
        queueDelayTimes.sortedBy { it.first }
        Logger.debug(
            LOG_ID,
            "Queue delay change is set to " + time + " for management " + managementId
        )

        nextDelayAlarm.cancel()
        nextDelayAlarm.schedule(
            object : java.util.TimerTask() {
                override fun run() = queueDelayAlarmHandler()
            },
            queueDelayTimes.get(0).first.getTime()
        )
        Logger.debug(
            LOG_ID,
            "New delay timer scheduled for " + queueDelayTimes.get(0).second + " to " +
                queueDelayTimes.get(0).first
        )
    }
    private fun queueDelayAlarmHandler() {
        Logger.debug(LOG_ID, "Starting delayed queue update routine...")
        if (queueDelayTimes.isEmpty()) return
        Logger.debug(LOG_ID, "Delayed update routine is ready")

        val (urgentQueueTime, urgentQueueManagementId) = queueDelayTimes.get(0)
        Logger.debug(LOG_ID, "Delayed update for management " + urgentQueueManagementId)

        // Check if the trigger is valid
        if (urgentQueueTime.getTime() >= Date().getTime()) {
            queueDelayTimes.removeAt(0)

            // Update the queue

            // Load the queue
            val queueId = databaseWrapper.getQueueIdOfManagement(urgentQueueManagementId)
            if (queueId == null) {
                Logger.internalError(LOG_ID, "Management has no Queue!")
                // Don't crash the server by a exception. This is just a log.
            } else {
                val queue = Queue(queueId, databaseWrapper)

                // Update the queue
                val prioritizationTime =
                    databaseWrapper.getManagementById(urgentQueueManagementId)!!
                        // managementId must exist
                        .settings
                        .prioritizationTime

                queue.updateQueue(prioritizationTime)
                Logger.debug(LOG_ID, "Delayed queue refreshed")

                // thi s will also set the next timer
                handleQueueUpdate(urgentQueueManagementId, queue)
            }
        } else if (queueDelayTimes.isNotEmpty()) {
            Logger.debug(LOG_ID, "Delayed queue is not ready yet")
            // Reset the next timer, if the trigger was invalid
            nextDelayAlarm.schedule(
                object : java.util.TimerTask() {
                    override fun run() = queueDelayAlarmHandler()
                },
                queueDelayTimes.get(0).first.getTime()
            )
            Logger.debug(
                LOG_ID,
                "Scheduled next queue for " + queueDelayTimes.get(0).second + " to " +
                    queueDelayTimes.get(0).first
            )
        }
        Logger.debug(LOG_ID, "Finished delayed queue update routine")
    }
}
