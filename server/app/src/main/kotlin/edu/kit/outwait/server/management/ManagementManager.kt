package edu.kit.outwait.server.management

import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIONamespace
import edu.kit.outwait.server.core.AbstractManager
import edu.kit.outwait.server.core.DatabaseWrapper
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
    }

    override fun bindSocket(socket: SocketIOClient, socketFacade: SocketFacade) {
        // Handle the login
        socketFacade.onReceive(Event.MANAGEMENT_LOGIN) { json ->
            val wrapper = (json as JSONCredentialsWrapper)
            val credentials = databaseWrapper.getManagementByUsername(wrapper.getUsername())

            if (wrapper.getPassword() != credentials.password) {
                socketFacade.send(Event.MANAGEMENT_LOGIN_DENIED, JSONEmptyWrapper())
                socketFacade.disconnect()
            } else {
                socketFacade.send(Event.MANAGEMENT_LOGIN_SUCCESS, JSONEmptyWrapper())

                // Create new management instance
                val manager = Management(socketFacade, credentials.id, databaseWrapper, this)
                managements.add(manager)
            }
        }

        // Handle the reset password function
        socketFacade.onReceive(Event.RESET_PASSWORD) { json ->
            resetManagementPassword((json as JSONResetPasswordWrapper).getUsername())
        }

        // Login request
        socketFacade.send(Event.LOGIN_REQUEST, JSONEmptyWrapper())
    }
    fun removeManagement(management: Management) {
        managements.remove(management)
    }
    fun beginTransaction(managementId: ManagementId): Queue? {
        if (activeTransactions.contains(managementId)) {
            return null;
        } else {
            activeTransactions.add(managementId)

            // Load the queue
            val queueId = databaseWrapper.getQueueIdOfManagement(managementId)
            val queue = Queue(managementId, queueId, databaseWrapper)

            return queue
        }
    }
    fun abortTransaction(managementId: ManagementId): Queue {
        assert(activeTransactions.contains(managementId))

        activeTransactions.remove(managementId)

        // Re-load the queue with the state before the transaction
        val queueId = databaseWrapper.getQueueIdOfManagement(managementId)
        databaseWrapper.deleteAllTemporarySlots(queueId) // delete all temporary slots
        val queue = Queue(managementId, queueId, databaseWrapper)

        return queue
    }

    /**
     * Is called internally when the queue has been changed somewhere. It will store the queue in
     * the database, inform all Managements and update the next-delay-alarm.
     */
    private fun handleQueueUpdate(managementId: ManagementId, queue: Queue) {
        queue.storeToDB(databaseWrapper)

        // Distribute the queue
        for (management in managements) {
            if (management.managementId == managementId) {
                management.sendUpdatedQueue(queue)
            }
        }

        // Create delay timer
        val nextDelayChange = queue.calculateNextDelayChange();
        if (nextDelayChange != null) {
            keepQueueDelayTime(nextDelayChange, managementId)
        }
    }

    fun saveTransaction(managementId: ManagementId, queue: Queue) {
        assert(activeTransactions.contains(managementId))

        handleQueueUpdate(managementId, queue)

        activeTransactions.remove(managementId)
    }
    fun updateManagementSettings(
        managementId: ManagementId,
        managementSettings: ManagementSettings
    ) {
        databaseWrapper.saveManagementSettings(managementSettings)

        for (management in managements) {
            if (management.managementId == managementId)
                management.sendUpdatedManagementSettings(managementSettings)
        }
    }
    private fun resetManagementPassword(username: String) {
        // TODO implement the reset password procedure
    }
    fun keepQueueDelayTime(time: Date, managementId: ManagementId) {
        queueDelayTimes.add(Pair(time, managementId))
        queueDelayTimes.sortedBy { it.first }

        nextDelayAlarm.cancel()
        nextDelayAlarm.schedule(
            object : java.util.TimerTask() {
                override fun run() = queueDelayAlarmHandler()
            },
            queueDelayTimes.get(0).first.getTime()
        )
    }
    private fun queueDelayAlarmHandler() {
        if (queueDelayTimes.isEmpty()) return

        val (urgentQueueTime, urgentQueueManagementId) = queueDelayTimes.get(0)

        // Check if the trigger is valid
        if (urgentQueueTime.getTime() >= Date().getTime()) {
            queueDelayTimes.removeAt(0)

            // Update the queue

            // Load the queue
            val queueId = databaseWrapper.getQueueIdOfManagement(urgentQueueManagementId)
            val queue = Queue(urgentQueueManagementId, queueId, databaseWrapper)

            // Update the queue
            val prioritizationTime =
                databaseWrapper.getManagementById(urgentQueueManagementId)
                    .settings
                    .prioritizationTime
            queue.updateQueue(prioritizationTime)

            handleQueueUpdate(urgentQueueManagementId, queue) // this will also set the next timer
        } else if (queueDelayTimes.isNotEmpty()) {
            // Reset the next timer, if the trigger was invalid
            nextDelayAlarm.schedule(
                object : java.util.TimerTask() {
                    override fun run() = queueDelayAlarmHandler()
                },
                queueDelayTimes.get(0).first.getTime()
            )
        }
    }
}
