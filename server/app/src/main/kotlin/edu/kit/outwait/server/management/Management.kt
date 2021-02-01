package edu.kit.outwait.management

class Management(socketFace: SocketFacade, managementId: ManagementId, databaseWrapper:DatabaseWrapper, managementManager: ManagementManager) {
    private managementManager: ManagementManager
    private managementInformation: ManagementInformation
    internal managementId: ManagementId
    private queue: Queue
    private socket: SocketFacade

    fun sendUpdatedQueue (queue: Queue):Unit {}
    fun sendUpdatedManagementSettings (managementSettings: ManagementSettings) {}
    private fun logout () {}
    private fun beginNewTransaction () {}
    private fun abortCurrentTransaction () {}
    private fun saveCurrentTransaction () {}
    private fun changeManagementSettings (managementSettings: ManagementSettings) {}
}
