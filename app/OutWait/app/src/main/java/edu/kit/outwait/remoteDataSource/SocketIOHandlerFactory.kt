package edu.kit.outwait.remoteDataSource

import edu.kit.outwait.clientDatabase.ClientInfoDao

/**
 * This class implements the interface or "abstract factory" HandlerFactory as a
 * "concrete factory" in the, here used and commonly known as, "abstract factory pattern".
 * It provides methods for the creation of the "concrete products" namely SocketIOHandlers.
 *
 */
class SocketIOHandlerFactory : HandlerFactory {

    /**
     * This method implements the abstract method "buildClientHandler" of the
     * "HandlerFactory" Interface and returns a concrete instance of
     * "SocketIOClientHandler", that implements the "ClientHandler" Interface
     *
     * @param dao of type ClientInfoDao, used to inject the client database (or its access) into
     * the SocketIOClientHandler (using Dependency Injection with hilt)
     * @return the created ClientHandler, of type SocketIOClientHandler
     */
    override fun buildClientHandler(dao: ClientInfoDao): ClientHandler {
        return SocketIOClientHandler(dao)
    }

    /**
     * This method implements the abstract method "buildManagementHandler" of the
     * "HandlerFactory" Interface and returns a concrete instance of "SocketIOManagementHandler",
     * that implements the "ManagementHandler" Interface
     *
     * @return the created ManagementHandler, of type SocketIOManagementHandler
     */
    override fun buildManagementHandler(): ManagementHandler {
        return SocketIOManagementHandler()
    }

}
