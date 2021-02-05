package elite.kit.outwait.remoteDataSource

import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URI

class ClientSocket {

    private val  serverURI = "http://127.0.0.1:8080/client"

    private val cSocket: Socket? = null

    init {
        val options = IO.Options()
        options.reconnection = true

        val cSocket = IO.socket(URI.create(serverURI), options)
    }
}

