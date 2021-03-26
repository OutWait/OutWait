package elite.kit.outwait.clientRepository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elite.kit.outwait.clientDatabase.ClientInfo
import elite.kit.outwait.clientDatabase.ClientInfoDao
import elite.kit.outwait.remoteDataSource.ClientHandler
import elite.kit.outwait.remoteDataSource.ClientServerErrors
import elite.kit.outwait.services.ServiceHandler
import io.mockk.*
import junit.framework.Assert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.joda.time.DateTime
import org.joda.time.Duration
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class ClientRepositoryTest {

    val testDispatcher = TestCoroutineDispatcher()
    val testCoroutineScope = TestCoroutineScope(testDispatcher)

    private lateinit var repository: ClientRepository
    private lateinit var internetAndDatabase: InternetAndDatabaseFake
    private lateinit var serviceHandler: ServiceHandler


    @get: Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp(){

        Dispatchers.setMain(testDispatcher)


        internetAndDatabase = InternetAndDatabaseFake()

        serviceHandler = mockk<ServiceHandler>(relaxed = true)

        repository = ClientRepository(internetAndDatabase, internetAndDatabase, serviceHandler)
    }

    @After
    fun tearDown(){
        Dispatchers.resetMain()
        testCoroutineScope.cleanupTestCoroutines()
    }

    @Test
    fun `when first slotCode is entered, the repo tells the Service Handler to start (with correct slot code)`()
    = runBlocking{
        repository.newCodeEntered(internetAndDatabase.VALID_CODE)

        verify(exactly = 1) { serviceHandler.startTimerService(any()) }
    }

    @Test
    fun `receives slot data`()
        = runBlocking{
        repository.getActiveSlots().observeForever {  }

        repository.newCodeEntered(internetAndDatabase.VALID_CODE)

        assertTrue(repository.getActiveSlots().value !== null)
        assertTrue(repository.getActiveSlots().value!!.isNotEmpty())
        assertEquals(internetAndDatabase.VALID_CODE, repository.getActiveSlots().value!!.first().slotCode)
    }

    @Test
    fun `repo keeps connection to server after successful slot code entry`()
        = runBlocking{
        assertFalse(repository.isConnectedToServer())
        repository.newCodeEntered(internetAndDatabase.VALID_CODE)
        assertTrue(repository.isConnectedToServer())
    }

    @Test
    fun `repo ends connection to server after slot is expired`()
        = runBlocking{
        repository.newCodeEntered(internetAndDatabase.VALID_CODE)
        internetAndDatabase.expireAllSlots()
        assertFalse(repository.isConnectedToServer())
    }


    /**
     * Simulates the ClientHandler of the remote data source and the Room database both
     * in one object; it implements both interfaces. We know that this is highly
     * Anti-Separation-Of-Concerns, but it allows us to create a realistic mock with
     * few code. The real Room database actualizes its live data of slots when the server
     * sends slot data. The InternetAndDatabaseFake also actualizes this Live Data.
     * Special Slot codes specify special reactions, for that look at the slot code constants
     * defined in this class
     *
     */
    private class InternetAndDatabaseFake : ClientHandler, ClientInfoDao {
        val VALID_CODE = "V"
        val INVALID_CODE = "I"

        fun expireAllSlots(){
            errorMessages.value = errorMessages.value !!+ ClientServerErrors.EXPIRED_SLOT_CODE
            clientInfoList.value = listOf()
        }

        override fun initCommunication(): Boolean {
            initCommunicationCalled = true
            return true
        }
        var initCommunicationCalled = false

        override fun endCommunication(): Boolean {
            initCommunicationCalled = false
            return true
        }

        override fun newCodeEntered(slotCode: String) {
            when (slotCode){
                VALID_CODE
                -> clientInfoList.value = clientInfoList.value!!.plus(
                    ClientInfo(
                        VALID_CODE,
                        "Test",
                        DateTime(),
                        DateTime(),
                        Duration(0),
                        Duration(0)
                    )
                )
            }
        }

        override fun refreshWaitingTime(slotCode: String) {
            TODO("Not yet implemented")
        }

        override fun getErrors(): LiveData<List<ClientServerErrors>>
            = errorMessages

        private val errorMessages = MutableLiveData<List<ClientServerErrors>>(listOf())


        override fun getAllClientInfoObservable(): LiveData<List<ClientInfo>> {
            return clientInfoList
        }
        private val clientInfoList = MutableLiveData<List<ClientInfo>>(listOf())

        override fun insert(clientInfo: ClientInfo) {
            throw NotImplementedError()
        }

        override fun update(clientInfo: ClientInfo) {
            throw NotImplementedError()
        }

        override fun getClientInfo(waitCode: String): ClientInfo? {
            throw NotImplementedError()
        }

        override fun deleteClientInfo(info: ClientInfo) {
            throw NotImplementedError()
        }

        override fun getClientInfoObservable(slotCode: String): LiveData<ClientInfo?> {
            throw NotImplementedError()
        }

        override fun getAllClientInfo(): List<ClientInfo> {
            throw NotImplementedError()
        }


        override fun clearTable() {
            throw NotImplementedError()
        }

    }
}
