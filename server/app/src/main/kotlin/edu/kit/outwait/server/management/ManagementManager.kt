package edu.kit.outwait.server.management

import com.corundumstudio.socketio.SocketIONamespace
import edu.kit.outwait.server.core.AbstractManager
import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.core.InternalServerErrorException
import edu.kit.outwait.server.core.Logger
import edu.kit.outwait.server.protocol.Event
import edu.kit.outwait.server.protocol.JSONCredentialsWrapper
import edu.kit.outwait.server.protocol.JSONEmptyWrapper
import edu.kit.outwait.server.protocol.JSONResetPasswordWrapper
import edu.kit.outwait.server.socketHelper.SocketFacade
import jakarta.mail.*
import jakarta.mail.internet.*
import java.util.Date
import java.util.Properties
import java.util.Timer

/**
 * Manager for all management instances (of connected institutions).
 *
 * It manages the login of managements, distributes updates between them, coordinates open
 * transactions and initiates delayed queue updates.
 *
 * @param namespace the SocketIO namespace for management, to configure the socket adapter.
 * @param databaseWrapper the DB to load and store queue and management data.
 * @constructor Initializes the object and registers all events.
 */
class ManagementManager(namespace: SocketIONamespace, databaseWrapper: DatabaseWrapper) :
    AbstractManager(namespace, databaseWrapper) {

    private val managements = mutableListOf<Management>()
    private val activeTransactions = hashSetOf<ManagementId>()
    private val queueDelayTimes = mutableListOf<Pair<Date, ManagementId>>()
    private var nextDelayAlarm = Timer()
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

    /**
     * Handles a new management connection.
     *
     * Initial message receivers are registered and a login request is sent.
     *
     * @param socketFacade the socketFacade of the new connection.
     */
    override fun bindSocket(socketFacade: SocketFacade) {
        // Add timeout
        val timeoutTimer = Timer()
        timeoutTimer.schedule(
            object : java.util.TimerTask() {
                override fun run() {
                    Logger.debug(LOG_ID, "Management connection login timed out")
                    socketFacade.disconnect()
                }
            },
            10000 // wait 10 seconds
        )

        // Handle the login
        socketFacade.onReceive(Event.MANAGEMENT_LOGIN) { json ->
            try {
                timeoutTimer.cancel()
            } catch (e: IllegalStateException) {
                // Timer has not been started jet. Ignore this
            }

            val wrapper = (json as JSONCredentialsWrapper)
            Logger.debug(LOG_ID, "New login of: " + wrapper)
            if (wrapper.getUsername() == "" || wrapper.getPassword() == "") {
                // Catch empty credentials
                Logger.debug(LOG_ID, "Access denied (empty credentials)")
                socketFacade.send(Event.MANAGEMENT_LOGIN_DENIED, JSONEmptyWrapper())
                socketFacade.disconnect()
            } else {
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
                    synchronized(this) { managements.add(manager) }
                }
            }
        }

        // Handle the reset password function
        socketFacade.onReceive(Event.RESET_PASSWORD) { json ->
            try {
                timeoutTimer.cancel()
            } catch (e: IllegalStateException) {
                // Timer has not been started jet. Ignore this
            }

            Logger.debug(LOG_ID, "Password resetting routine started")
            resetManagementPassword((json as JSONResetPasswordWrapper).getUsername())
        }

        // Login request
        Logger.debug(LOG_ID, "Starting login routine")
        socketFacade.send(Event.LOGIN_REQUEST, JSONEmptyWrapper())
    }

    /**
     * Removes a management from the internal list.
     *
     * Called by a management on logout. Started transactions are aborted implicitly.
     *
     * @param management the management to remove.
     */
    fun removeManagement(management: Management) {
        Logger.debug(LOG_ID, "Removing management connection")
        // Close open transactions
        if (management.isTransactionRunning()) management.abortCurrentTransaction()

        synchronized(this) {
            managements.remove(management)

            if (managements.isEmpty()) {
                Logger.debug(LOG_ID, "Last active management connection closed.");
            }
        }
    }

    /**
     * Starts a new transaction.
     *
     * If the requested transaction is not available (because another management of the same
     * institution has already running transaction), null is returned. Otherwise a queue is
     * returned, that represent the new transaction.
     *
     * @param managementId the id of the institution whose management wants to start a new
     *     transaction.
     * @return The initialized queue of the new transaction or null on error.
     * @throws InternalServerErrorException when the management has not corresponding queue.
     */
    @Throws(InternalServerErrorException::class)
    fun beginTransaction(managementId: ManagementId): Queue? {
        synchronized(this) {
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
                    throw InternalServerErrorException("Management has no corresponding queue.")
                } else {
                    Logger.debug(LOG_ID, "New transaction queue loaded")
                    return Queue(queueId, databaseWrapper)
                }
            }
        }
    }

    /**
     * Aborts a running transaction.
     *
     * The old queue (the one with the state before the transaction) is returned.
     *
     * @param managementId the id of the institution whose running transaction should be aborted.
     * @return The old queue (before the transaction) or null on error.
     * @throws InternalServerErrorException when the management has not corresponding queue or
     *     temporarily created slot couldn't be deleted.
     */
    @Throws(InternalServerErrorException::class)
    fun abortTransaction(managementId: ManagementId): Queue {
        synchronized(this) {
            Logger.debug(LOG_ID, "Aborting transaction of management " + managementId + "...")
            assert(activeTransactions.contains(managementId))

            activeTransactions.remove(managementId)
            Logger.debug(LOG_ID, "Transaction aborted.")
            Logger.debug(LOG_ID, "Active transaction removed")
        }

        // Re-load the queue with the state before the transaction
        val queueId = databaseWrapper.getQueueIdOfManagement(managementId)
        if (queueId == null) {
            Logger.internalError(LOG_ID, "Management has no Queue!")
            throw InternalServerErrorException("Management has no corresponding queue.")
        } else {
            // delete all temporary slots
            Logger.debug(LOG_ID, "Deleting temporary slots...")
            if (!databaseWrapper.deleteAllTemporarySlots(queueId)) {
                throw InternalServerErrorException("Can't delete temporarily created slots.")
            }
            Logger.debug(LOG_ID, "Temporary slots deleted.")
            return Queue(queueId, databaseWrapper)
        }
    }

    /**
     * Is called internally when the queue has been changed somewhere.
     *
     * It will store the queue in the database, inform all Managements and update the
     * next-delay-alarm.
     *
     * @param managementId the id of the institution whose managements should receive the update.
     * @param queue the new queue.
     * @throws InternalServerErrorException when the queue could not be saved.
     */
    @Throws(InternalServerErrorException::class)
    private fun handleQueueUpdate(managementId: ManagementId, queue: Queue) {
        Logger.debug(
            LOG_ID,
            "Saving updated queue for management " + managementId + ", new queue: " + queue
        )
        if (!queue.storeToDB(databaseWrapper)) {
            throw InternalServerErrorException("Failed to save the queue into the database.")
        }

        // Distribute the queue
        synchronized(this) {
            for (management in managements) {
                if (management.managementId == managementId) {
                    management.sendUpdatedQueue(queue)
                }
            }
        }

        // Create delay timer
        Logger.debug(LOG_ID, "Saving queue delay change for later")
        val nextDelayChange = queue.calculateNextDelayChange()
        if (nextDelayChange != null) {
            keepQueueDelayTime(nextDelayChange, managementId)
        }
    }

    /**
     * Saves a running transaction.
     *
     * This will inform all active managements of this institution.
     *
     * @param managementId the id of the institution whose running transaction should be saved.
     * @return The old queue (before the transaction) or null on error.
     * @throws InternalServerErrorException when the queue could not be saved.
     */
    @Throws(InternalServerErrorException::class)
    fun saveTransaction(managementId: ManagementId, queue: Queue) {
        synchronized(this) {
            Logger.debug(LOG_ID, "Saving transaction. Checking...")
            assert(activeTransactions.contains(managementId))
            Logger.debug(LOG_ID, "Check done.")

            handleQueueUpdate(managementId, queue)

            activeTransactions.remove(managementId)
            Logger.debug(LOG_ID, "Active transaction removed")
        }
    }

    /**
     * Notifies all management of the given institution about the updated settings.
     *
     * This will also store the new settings in the DB.
     *
     * @param managementId the id of the institution whose settings have been changed.
     * @param managementSettings the new settings of the management.
     * @throws InternalServerErrorException when the settings could not be saved.
     */
    @Throws(InternalServerErrorException::class)
    fun updateManagementSettings(
        managementId: ManagementId,
        managementSettings: ManagementSettings
    ) {
        Logger.debug(
            LOG_ID,
            "Updating management settings of " + managementId + " with settings: " +
                managementSettings
        )

        if (!databaseWrapper.saveManagementSettings(managementId, managementSettings)) {
            throw InternalServerErrorException("Failed to save the settings into the database.")
        }

        synchronized(this) {
            for (management in managements) {
                if (management.managementId == managementId)
                    management.sendUpdatedManagementSettings(managementSettings)
            }
        }
    }

    /**
     * Initiates the reset routine for the password of a management.
     *
     * @param username the login name of the institution whose password should be reset.
     */
    private fun resetManagementPassword(username: String) {
        Logger.debug(LOG_ID, "Reset password routine started")

        val managementId = databaseWrapper.getManagementByUsername(username)?.id;
        if (managementId == null) {
            Logger.debug(LOG_ID, "User is not valid: " + username)
            return // Not a valid account
        }

        val email = databaseWrapper.getManagementById(managementId)?.details?.email;

        if (email == null || !email.contains('@')) {
            Logger.debug(LOG_ID, "Email/user is not valid: " + email + " for user " + username)
            return // Not a valid account/email
        }

        // Generate a new random password
        var newPassword = ""
        var allowedCharacters =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrtsuvwxyz!\"§$%&/()=?{[]}\\" +
                "-_.:,;#'+*~<>|^´`"
        for (i in 0..10) {
            newPassword += allowedCharacters[(Math.random() * allowedCharacters.length).toInt()]
        }

        // Send the email with the new password
        val props = Properties();
        props.put("mail.smtp.host", "localhost");
        val session = Session.getInstance(props, null);
        try {
            var msg = MimeMessage(session);
            msg.setFrom("service@noreply.outwait.com");
            msg.setRecipients(Message.RecipientType.TO, email);
            msg.setSubject("OutWait service: Your password has been reset!");
            msg.setSentDate(Date());
            msg.setText(
                "Dear " + username + ",\n\nThe password for your OutWait account '" + username +
                    "' has been reset. Your new password is:\n" + newPassword +
                    "\n\nIf you did not try to reset your password, please contact our service " +
                    "team. Please do not reply to this email directly.\n\nBest regards, The " +
                    "OutWait team\n"
            );
            Transport.send(msg);

            // Reset the password in the database
            databaseWrapper.changeManagementPassword(username, newPassword)
        } catch (e:MessagingException) {
            Logger.error(
                LOG_ID,
                "Failed to send email for user " + username + " - " + email + ". With error: " + e
            );
        }
    }

    /**
     * Initiates a timer to update a queue at the given time.
     *
     * @param time the time at which the queue should be updated.
     * @param managementId the id of the institution whose queue should be updated later.
     */
    private fun keepQueueDelayTime(time: Date, managementId: ManagementId) {
        synchronized(this) {
            queueDelayTimes.removeIf { it.second == managementId } // remove previous timers
            queueDelayTimes.add(Pair(time, managementId))
            queueDelayTimes.sortBy { it.first }
            Logger.debug(
                LOG_ID,
                "Queue delay change is set to " + time + " for management " + managementId
            )
            Logger.debug(
                LOG_ID,
                "Currently " + queueDelayTimes.size + " delay changes are tracked:"
            )
            for (i in 0 until queueDelayTimes.size)
                Logger.debug(
                    LOG_ID,
                    "time " + queueDelayTimes[i].first + " for management " +
                        queueDelayTimes[i].second
                )

            try {
                nextDelayAlarm.cancel()
                nextDelayAlarm = Timer()
            } catch (e: java.lang.IllegalStateException) {
                // Timer has not been started jet. Ignore this
                Logger.debug(LOG_ID, "Timer already cancelled")
            }
            try {
                nextDelayAlarm.schedule(
                    object : java.util.TimerTask() {
                        override fun run() = queueDelayAlarmHandler()
                    },
                    queueDelayTimes.get(0).first
                )
            } catch (e: java.lang.IllegalStateException) {
                // Timer has not been started jet. Ignore this
                Logger.debug(LOG_ID, "Timer already cancelled (in re-schedule)")
            }
            Logger.debug(
                LOG_ID,
                "New delay timer scheduled for " + queueDelayTimes.get(0).second + " to " +
                    queueDelayTimes.get(0).first
            )
        }
    }

    /**
     * The routine that is called to update a queue.
     *
     * It will update the queue whose update delay time is the youngest. The timer will be reset to
     * update the other queues as needed.
     */
    private fun queueDelayAlarmHandler() {
        synchronized(this) {
            Logger.debug(LOG_ID, "Starting delayed queue update routine...")
            if (queueDelayTimes.isEmpty()) return
            Logger.debug(LOG_ID, "Delayed update routine is ready")

            val (urgentQueueTime, urgentQueueManagementId) = queueDelayTimes.get(0)
            queueDelayTimes.removeAt(0)
            Logger.debug(LOG_ID, "Delayed update for management " + urgentQueueManagementId)

            // Check if the trigger is valid
            if (urgentQueueTime.getTime() <= Date().getTime()) {

                // Update the queue

                // Load the queue
                val queueId = databaseWrapper.getQueueIdOfManagement(urgentQueueManagementId)
                if (queueId == null) {
                    Logger.internalError(LOG_ID, "Management has no Queue!")
                    // Silently ignore this error as it is not the result of a manager request
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

                    // this will also set the next timer
                    try {
                        handleQueueUpdate(urgentQueueManagementId, queue)
                    } catch (e: InternalServerErrorException) {
                        Logger.internalError(
                            LOG_ID,
                            "InternalServerErrorException while updating the delayed queues, with" +
                                " " + "message: " + e.message
                        )

                        // Send the error message to all corresponding managements
                        managements.filter { it.managementId == urgentQueueManagementId }
                            .forEach {
                                it.sendInternalErrorMessage(
                                    "Failed to update the queue with an overdue slot. Reason: " +
                                        e.message!!
                                )
                            }

                        // Ensure that the next timer is set
                        try {
                            nextDelayAlarm.schedule(
                                object : java.util.TimerTask() {
                                    override fun run() = queueDelayAlarmHandler()
                                },
                                queueDelayTimes.get(0).first.getTime()
                            )
                        } catch (e: java.lang.IllegalStateException) {
                            // Timer has not been started jet. Ignore this
                            Logger.debug(LOG_ID, "Timer already cancelled (in re-schedule)")
                        }
                    }
                }
            } else if (queueDelayTimes.isNotEmpty()) {
                Logger.debug(LOG_ID, "Delayed queue is not ready yet")
                // Reset the next timer, if the trigger was invalid
                try {
                    nextDelayAlarm.schedule(
                        object : java.util.TimerTask() {
                            override fun run() = queueDelayAlarmHandler()
                        },
                        queueDelayTimes.get(0).first.getTime()
                    )
                } catch (e: java.lang.IllegalStateException) {
                    // Timer has not been started jet. Ignore this
                    Logger.debug(LOG_ID, "Timer already cancelled (in re-schedule)")
                }
                Logger.debug(
                    LOG_ID,
                    "Scheduled next queue for " + queueDelayTimes.get(0).second + " to " +
                        queueDelayTimes.get(0).first
                )
            }
            Logger.debug(LOG_ID, "Finished delayed queue update routine")
        }
    }
}
