package elite.kit.outwait.remoteDataSource

import io.socket.client.IO
import io.socket.client.Socket
import java.net.URI

class ManagementSocket() {

    //TODO Nur ein Socket und serverURI in Konstruktor

    private val  serverURI = "http://127.0.0.1:8080/management"

    private val mSocket: Socket? = null

    init {
        val options = IO.Options()
        options.reconnection = true

        val mSocket = IO.socket(URI.create(serverURI), options)
    }

    fun initializeConnection() {
        //TODO Param für EventListener

        registerEventListeners()
        mSocket?.connect()

        //TODO Was ist mit onConnectEvent?
    }


    private fun registerEventListeners() {
        //TODO: Wie werden die/der Callback übergeben mit den Events?
        //TODO: Ein Callback und Strategie im konkreten Handler
        //TODO: Public oder privat?
    }

    fun releaseConnection() {
        mSocket?.close()
    }
}
