package edu.kit.outwait.management

class ManagementManager(namespace: SocketIONamespace, databaseWrapper: DatabaseWrapper) : core.AbstractManager {
    private managements: List<Management>
    private activeTransactions: HashSet<ManagementId>
    private queueDelayTimes: List<Pair<Date, ManagementId>>
    private nextDelayAlarm: Timer

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
