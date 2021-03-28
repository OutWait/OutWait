package elite.kit.outwait.remoteDataSource

import elite.kit.outwait.customDataTypes.Mode
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.networkProtocol.Event
import io.mockk.*
import org.joda.time.DateTime
import org.joda.time.Duration

import org.junit.Before
import org.junit.Test

class SocketIOManagementHandlerTest {

    private val slotCode1 = "123456789"
    private val slotCode2 = "987654321"
    private val username = "Dr. Diarrhoea"
    private val password = "Let it go"

    @Before
    fun setUp() {

        // set up the mocked SocketAdapter by mocking its constructor
        mockkConstructor(SocketAdapter::class)
        every { anyConstructed<SocketAdapter>().initializeConnection(any()) } returns true
        every { anyConstructed<SocketAdapter>().emitEventToServer(any(), any())} just runs
        every { anyConstructed<SocketAdapter>().releaseConnection() } just runs
    }

    @Test
    fun initCommAndEndTest() {

        val managementHandler = SocketIOManagementHandler()

        managementHandler.initCommunication()
        verify {
            anyConstructed<SocketAdapter>().initializeConnection(any())
        }

        managementHandler.endCommunication()
        verify {
            anyConstructed<SocketAdapter>().releaseConnection()
        }
    }

    @Test
    fun changePreferencesTest() {
        val fakePrefs = Preferences(Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO, Mode.ONE)
        val eventString = Event.CHANGE_MANAGEMENT_SETTINGS.getEventString()

        val managementHandler = SocketIOManagementHandler()
        managementHandler.initCommunication()
        managementHandler.changePreferences(fakePrefs)
        verify {
        anyConstructed<SocketAdapter>().emitEventToServer(eventString, any())
        }
    }

    @Test
    fun resetPasswordTest() {
        val username = ""
        val eventString = Event.RESET_PASSWORD.getEventString()

        val managementHandler = SocketIOManagementHandler()
        managementHandler.initCommunication()
        managementHandler.resetPassword(username)
        verify {
            anyConstructed<SocketAdapter>().emitEventToServer(eventString, any())
        }
    }

    @Test
    fun endCurrentSlotTest() {
        val eventString = Event.END_CURRENT_SLOT.getEventString()
        val managementHandler = SocketIOManagementHandler()
        managementHandler.initCommunication()
        managementHandler.endCurrentSlot()
        verify {
            anyConstructed<SocketAdapter>().emitEventToServer(eventString, any())
        }
    }

    @Test
    fun startTransactionTest() {
        val eventString = Event.START_TRANSACTION.getEventString()
        val managementHandler = SocketIOManagementHandler()
        managementHandler.initCommunication()
        managementHandler.startTransaction()
        verify {
            anyConstructed<SocketAdapter>().emitEventToServer(eventString, any())
        }
    }

    @Test
    fun loginWithoutRequest() {
        val eventString = Event.MANAGEMENT_LOGIN.getEventString()
        val managementHandler = SocketIOManagementHandler()
        managementHandler.initCommunication()
        managementHandler.login(username, password)
        verify (exactly = 0) {
            anyConstructed<SocketAdapter>().emitEventToServer(eventString, any())
        }
    }

    @Test
    fun addSpontSlotTest() {
        val eventString = Event.ADD_SPONTANEOUS_SLOT.getEventString()
        val timeOfCreation = DateTime.now()
        val duration = Duration(Duration.ZERO)

        val managementHandler = SocketIOManagementHandler()
        managementHandler.initCommunication()
        managementHandler.addSpontaneousSlot(duration, timeOfCreation)
        verify {
            anyConstructed<SocketAdapter>().emitEventToServer(eventString, any())
        }
    }

    @Test
    fun changeSlotDuratioTest() {
        val eventString = Event.CHANGE_SLOT_DURATION.getEventString()
        val duration = Duration.ZERO
        val managementHandler = SocketIOManagementHandler()
        managementHandler.initCommunication()
        managementHandler.changeSlotDuration(slotCode1, duration)
        verify {
            anyConstructed<SocketAdapter>().emitEventToServer(eventString, any())
        }
    }

    @Test
    fun changeFixedSlotTimeTest() {
        val eventString = Event.CHANGE_FIXED_SLOT_TIME.getEventString()
        val newTime = DateTime.now()
        val managementHandler = SocketIOManagementHandler()
        managementHandler.initCommunication()
        managementHandler.changeFixedSlotTime(slotCode1, newTime)
        verify {
            anyConstructed<SocketAdapter>().emitEventToServer(eventString, any())
        }
    }

    @Test
    fun moveSlotAfterAnotherTest() {
        val eventString = Event.MOVE_SLOT_AFTER_ANOTHER.getEventString()
        val managementHandler = SocketIOManagementHandler()
        managementHandler.initCommunication()
        managementHandler.moveSlotAfterAnother(slotCode1, slotCode2)
        verify {
            anyConstructed<SocketAdapter>().emitEventToServer(eventString, any())
        }
    }

    @Test
    fun addFixedSlotTest() {
        val eventString = Event.ADD_FIXED_SLOT.getEventString()
        val duration = Duration.ZERO
        val appointmentTime = DateTime.now()
        val managementHandler = SocketIOManagementHandler()
        managementHandler.initCommunication()
        managementHandler.addFixedSlot(duration, appointmentTime)
        verify {
            anyConstructed<SocketAdapter>().emitEventToServer(eventString, any())
        }
    }

    @Test
    fun logoutTest() {
        val eventString = Event.MANAGEMENT_LOGOUT.getEventString()
        val managementHandler = SocketIOManagementHandler()
        managementHandler.initCommunication()
        managementHandler.logout()
        verify {
            anyConstructed<SocketAdapter>().emitEventToServer(eventString, any())
        }
    }

    @Test
    fun loginWithoutInit() {
        val eventString = Event.MANAGEMENT_LOGIN.getEventString()
        val managementHandler = SocketIOManagementHandler()
        managementHandler.login(username, password)
        verify (exactly = 0){
            anyConstructed<SocketAdapter>().emitEventToServer(eventString, any())
        }
    }
}
