package edu.kit.outwait.server.management

import java.util.Date
import java.util.Timer

import com.corundumstudio.socketio.SocketIONamespace
import com.corundumstudio.socketio.SocketIOClient

import edu.kit.outwait.server.core.AbstractManager
import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.socketHelper.SocketFacade

class ManagementManager(namespace: SocketIONamespace, databaseWrapper: DatabaseWrapper) : AbstractManager( databaseWrapper ) {
    private val managements = listOf<Management>()
    private val activeTransactions = hashSetOf<ManagementId>()
    private val queueDelayTimes = listOf<Pair<Date, ManagementId>>()
    private val nextDelayAlarm = Timer()

    fun bindSocket(socket: SocketIOClient, socketFacade: SocketFacade) {}
    fun removeManagement(management: Management) {}
    fun beginTransaction(managementId: ManagementId): Queue? { return null }
    fun abortTransaction(managementId: ManagementId): Queue { return Queue(managementId, QueueId(0), databaseWrapper) }
    fun saveTransaction(managementId: ManagementId, queue: Queue) {}
    fun updateManagementSettings(managementId: ManagementId, managementSettings: ManagementSettings) {}
    private fun resetManagementPassword(username: String) {}
    fun keepQueueDelayTime(time: Date, managementId: ManagementId) {}
    private fun queueDelayAlarmHandler() {}
}
