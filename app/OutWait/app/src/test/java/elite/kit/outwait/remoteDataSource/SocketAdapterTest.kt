package elite.kit.outwait.remoteDataSource

import elite.kit.outwait.networkProtocol.Event
import elite.kit.outwait.networkProtocol.SLOT_CODE
import io.mockk.every
import io.mockk.mockk
import io.socket.client.Socket
import io.mockk.*
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class SocketAdapterTest {

    private val namespace = "spacename"
    private val slotCode = "123456789"
    private val jsonSlotCode = JSONObject().put(SLOT_CODE, slotCode)

    @Test
    fun failedInitComm() {
        mockkConstructor(Socket::class)
        every { anyConstructed<Socket>().open() } returns null
        every { anyConstructed<Socket>().connect() } returns null

        val socketAdapter = SocketAdapter(namespace)
        assertEquals(false, socketAdapter.initializeConnection(hashMapOf()))
    }

    @Test
    fun registerListenersTest() {
        mockkConstructor(Socket::class)
        every { anyConstructed<Socket>().open() } returns Socket(mockk(), null, mockk())
        every { anyConstructed<Socket>().connect() } returns Socket(mockk(), null, mockk())

        val socketAdapter = SocketAdapter(namespace)
        socketAdapter.initializeConnection(hashMapOf())
        verify {
            anyConstructed<Socket>().on(any(), any())
        }
    }


    @Test
    fun testWrapping1() {
        mockkConstructor(Socket::class)
        every { anyConstructed<Socket>().open() } returns Socket(mockk(), null, mockk())
        every { anyConstructed<Socket>().connect() } returns Socket(mockk(), null, mockk())
        val eventString = Event.REFRESH_SLOT_APPROX.getEventString()

        val wrapped = Event.REFRESH_SLOT_APPROX.createWrapper(jsonSlotCode)
        val socketAdapter = SocketAdapter(namespace)
        socketAdapter.emitEventToServer(eventString, wrapped)
        verify {
            anyConstructed<Socket>().emit(eventString, jsonSlotCode.toString())
        }
    }

    @Test
    fun testWrapping2() {
        mockkConstructor(Socket::class)
        every { anyConstructed<Socket>().open() } returns Socket(mockk(), null, mockk())
        every { anyConstructed<Socket>().connect() } returns Socket(mockk(), null, mockk())
        val eventString = Event.ABORT_TRANSACTION.getEventString()
        val data = JSONObject()
        val wrapped = Event.ABORT_TRANSACTION.createWrapper(data)

        val socketAdapter = SocketAdapter(namespace)
        socketAdapter.emitEventToServer(eventString, wrapped)
        verify {
            anyConstructed<Socket>().emit(eventString, data.toString())
        }
    }

}
