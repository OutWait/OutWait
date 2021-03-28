package elite.kit.outwait.remoteDataSource

import elite.kit.outwait.clientDatabase.ClientInfoDao
import elite.kit.outwait.networkProtocol.*
import io.mockk.*

import org.junit.Test

class SocketIOClientHandlerTest {

    @Test
    fun initCommAndEnd() {
        mockkConstructor(SocketAdapter::class)
        every { anyConstructed<SocketAdapter>().initializeConnection(any()) } returns true
        every { anyConstructed<SocketAdapter>().releaseConnection() } just runs

        val clientHandler = SocketIOClientHandler(mockk())

        clientHandler.initCommunication()
        verify {
            anyConstructed<SocketAdapter>().initializeConnection(any())
        }

        clientHandler.endCommunication()
        verify {
            anyConstructed<SocketAdapter>().releaseConnection()
        }
    }

    @Test
    fun listenNewSlotCalled() {
        val slotCode = "123456789"
        val eventString = Event.LISTEN_SLOT.getEventString()

        val mockedDao = mockk<ClientInfoDao>()

        mockkConstructor(SocketAdapter::class)
        every { anyConstructed<SocketAdapter>().initializeConnection(any()) } returns true
        every { anyConstructed<SocketAdapter>().emitEventToServer(eventString, any())} just runs
        every { anyConstructed<SocketAdapter>().releaseConnection() } just runs

        val clientHandler = SocketIOClientHandler(mockedDao)

        clientHandler.initCommunication()
        clientHandler.newCodeEntered(slotCode)

        verify (exactly = 1) {
            anyConstructed<SocketAdapter>().emitEventToServer(eventString, any())
        }
    }

    @Test
    fun refreshSlotApproxCalled() {
        val slotCode = "123456789"
        val eventString = Event.REFRESH_SLOT_APPROX.getEventString()

        val mockedDao = mockk<ClientInfoDao>(relaxed = true)
        mockkConstructor(SocketAdapter::class)
        every { anyConstructed<SocketAdapter>().initializeConnection(any()) } returns true
        every { anyConstructed<SocketAdapter>().emitEventToServer(eventString, any())} just runs
        every { anyConstructed<SocketAdapter>().releaseConnection() } just runs

        val clientHandler = SocketIOClientHandler(mockedDao)
        clientHandler.initCommunication()
        clientHandler.refreshWaitingTime(slotCode)

        verify (exactly = 1) {
            anyConstructed<SocketAdapter>().emitEventToServer(eventString, any())
        }
    }


}
