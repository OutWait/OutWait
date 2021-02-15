package elite.kit.outwait.remoteDataSource

import elite.kit.outwait.clientDatabase.ClientInfoDao

interface HandlerFactory {

    fun buildClientHandler(dao: ClientInfoDao): ClientHandler

    fun buildManagementHandler(): ManagementHandler
}
