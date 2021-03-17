package edu.kit.outwait.server.client

import edu.kit.outwait.server.socketHelper.SocketFacade
import io.mockk.mockk

class ClientTest {

    @org.junit.jupiter.api.BeforeEach
    fun setUp() {
        val socketFacadeMock = mockk<SocketFacade>()
    }

    @org.junit.jupiter.api.Test
    internal fun testEndSlot() {
    }
}
