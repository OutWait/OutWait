package edu.kit.outwait.remoteDataSource

import edu.kit.outwait.clientDatabase.ClientInfoDao

/**
 * This interface "HandlerFactory" represents the "abstract factory"
 * in the, here used and commonly known as, "abstract factory pattern".
 * It describes the provided methods for the instantiation of the "products"
 * (in this case the Client- and ManagementHandler) that the
 * "concrete factories" have to implement
 *
 */
interface HandlerFactory {

    /**
     * This abstract method builds and returns a ClientHandler
     *
     * @param dao of type ClientInfoDao, used for the injection of the client database
     * into the ClientHandler (using Dependency Injection with hilt)
     * @return the created ClientHandler
     */
    fun buildClientHandler(dao: ClientInfoDao): ClientHandler

    /**
     * This abstract method builds and returns a ManagementHandler
     *
     * @return the created ManagementHandler
     */
    fun buildManagementHandler(): ManagementHandler
}
