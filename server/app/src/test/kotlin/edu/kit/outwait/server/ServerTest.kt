package edu.kit.outwait.server

import edu.kit.outwait.server.core.Server;
import edu.kit.outwait.server.protocol.*;
import edu.kit.outwait.server.protocol.Event;
import edu.kit.outwait.server.slot.SlotCode;
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.net.URI

class SocketMock {
    val options = IO.Options ();
    val mSocket = IO.socket(URI.create("http://127.0.0.1:8080/client"), options);

    init {
        mSocket.on(Socket.EVENT_CONNECT, Emitter.Listener { println("Callback EVENT_CONNECT") })

        mSocket.on(
            Socket.EVENT_CONNECT_ERROR,
            Emitter.Listener {
                println("Callback EVENT_CONNECT_ERROR")
                println("Callback EVENT_CONNECT_ERROR")
            }
        )
        mSocket.on(Socket.EVENT_ERROR, Emitter.Listener { println("Callback EVENT_ERROR") })
        mSocket.on(
            Socket.EVENT_CONNECT_TIMEOUT,
            Emitter.Listener { println("Callback EVENT_CONNECT_TIMEOUT") }
        )

        mSocket.on(
            Socket.EVENT_DISCONNECT,
            Emitter.Listener { println("Callback EVENT_DISCONNECT") }
        )

        for (e in Event.values()) {
            mSocket.on(
                e.getEventTag(),
                Emitter.Listener {
                    println(
                        "Incoming event " + e.getEventTag() + " with data of size " + it.size + ":"
                    )
                    for (d in it) println(d)
                    // The mocking stuff
                    if (e == Event.READY_TO_SERVE) {
                        val wrapper = JSONSlotCodeWrapper()
                        wrapper.setSlotCode(SlotCode("123456"))
                        mSocket.emit(Event.LISTEN_SLOT.getEventTag(), wrapper.getJSONString())
                        println("Send listen request")
                    }
                }
            )
        }

        mSocket.connect();
    }
}

class ServerTest {
    //@Test
    fun testClientConnection() {
        java.util
            .Timer()
            .schedule(
                object : java.util.TimerTask() {
                    override fun run() {
                        val socket = SocketMock()
                    }
                },
                5000
            )

        val server = Server()
        server.run()
    }
}

fun main() {
    ServerTest().testClientConnection()
}
