package elite.kit.outwait.remoteDataSource

interface HandlerFactory {

    fun buildClientHandler(): ClientHandler

    fun buildManagementHandler(): ManagementHandler
}
