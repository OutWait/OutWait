package edu.kit.outwait.server.server

import kotlin.test.Test
import kotlin.test.assertNotNull

class ServerTest {
    @Test fun testServerHasAGreeting() {
        val classUnderTest = Server()
        assertNotNull(classUnderTest.greeting, "server should have a greeting")
    }
}
