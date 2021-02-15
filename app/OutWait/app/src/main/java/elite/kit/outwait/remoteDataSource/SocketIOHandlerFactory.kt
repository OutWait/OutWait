package elite.kit.outwait.remoteDataSource

import elite.kit.outwait.clientDatabase.ClientInfoDao

class SocketIOHandlerFactory : HandlerFactory {

    override fun buildClientHandler(dao: ClientInfoDao): ClientHandler {
        return SocketIOClientHandler(dao)
    }

    override fun buildManagementHandler(): ManagementHandler {
        return SocketIOManagementHandler()
    }

}
