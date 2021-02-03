package elite.kit.outwait.remoteDataSource

class SocketIOHandlerFactory : HandlerFactory {

    override fun buildClientHandler(): ClientHandler {
        return SocketIOClientHandler()
    }

    override fun buildManagementHandler(): ManagementHandler {
        return SocketIOManagementHandler()
    }

}
