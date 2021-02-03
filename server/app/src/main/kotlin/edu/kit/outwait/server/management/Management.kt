package edu.kit.outwait.server.management

import edu.kit.outwait.server.core.DatabaseWrapper
import edu.kit.outwait.server.socketHelper.SocketFacade
import java.time.Duration

class Management(
    private val socketFace: SocketFacade,
    internal val managementId: ManagementId,
    databaseWrapper:DatabaseWrapper,
    private val managementManager: ManagementManager
) {
    private val managementInformation: ManagementInformation
    private var queue: Queue? = null

    init {
        // TODO load real data
        managementInformation =
            ManagementInformation(
                ManagementDetails(""),
                ManagementSettings(
                    Mode.ONE,
                    Duration.ZERO,
                    Duration.ZERO,
                    Duration.ZERO,
                    Duration.ZERO
                )
            )
    }

    fun sendUpdatedQueue (queue: Queue):Unit {}
    fun sendUpdatedManagementSettings (managementSettings: ManagementSettings) {}
    private fun logout () {}
    private fun beginNewTransaction () {}
    private fun abortCurrentTransaction () {}
    private fun saveCurrentTransaction () {}
    private fun changeManagementSettings (managementSettings: ManagementSettings) {}
}
