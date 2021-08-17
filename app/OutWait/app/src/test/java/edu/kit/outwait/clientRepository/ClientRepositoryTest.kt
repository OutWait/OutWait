package edu.kit.outwait.clientRepository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import edu.kit.outwait.clientDatabase.ClientInfo
import edu.kit.outwait.clientDatabase.ClientInfoDao
import edu.kit.outwait.remoteDataSource.ClientHandler
import edu.kit.outwait.remoteDataSource.ClientServerErrors
import edu.kit.outwait.services.ServiceHandler
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
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

/**
 * Tests the behaviour of the client repository. Contains an advanced Fake
 * for "the world", or more exactly for the internet and the database. So the
 * complete backend is mocked and faked and this is a pure unit test.
 *
 */
class ClientRepositoryTest {

    //Coroutine Test Environment setup
    private val testDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testDispatcher)
    @get: Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    //Mocks & Fakes
    private lateinit var webDBFake: InternetAndDatabaseFake
    private lateinit var serviceHandler: ServiceHandler

    //the subject of the test
    private lateinit var repo: ClientRepository



    @Before
    fun setUp(){
        //Coroutine Test Environment setup
        Dispatchers.setMain(testDispatcher)

        //Mocks & Fakes setUp
        /*
        we surround our Fake with a "spyk" so we are able to proof if certain methods
        were called or not (we can just have all the cool mokk functions for our own
        Fake)
         */

        webDBFake = spyk(InternetAndDatabaseFake())
        serviceHandler = mockk<ServiceHandler>(relaxed = true)

        //test subject setup
        repo = ClientRepository(webDBFake, webDBFake, serviceHandler)
    }

    @After
    fun tearDown(){
        //Coroutine Test Environment clean up
        Dispatchers.resetMain()
        testCoroutineScope.cleanupTestCoroutines()
    }

    /**
     * when first slotCode is entered, the repo tells the Service Handler to start
     * the background actualization, but only if the entered code is correct
     * (elsewise there is no slot we are interested to observe)
     *
     */
    @Test
    fun `valid wait code entered - background service started`() = runBlocking{
        repo.newCodeEntered(InternetAndDatabaseFake.VALID_CODE)

        verify(exactly = 1) { serviceHandler.startTimerService(repo) }
    }

    /**
     * when first slotCode is entered, the repo tells the Service Handler to start
     * the background actualization, but only if the entered code is correct
     * (elsewise there is no slot we are interested to observe)
     *
     */
    @Test
    fun `invalid wait code entered - background service not started`() = runBlocking{
        repo.newCodeEntered(InternetAndDatabaseFake.INVALID_CODE)

        verify(exactly = 0) { serviceHandler.startTimerService(any()) }
    }

    /**
     * After a successful wait code entry, the repo should actualize its live
     * data with the received information from the server.
     *
     */
    @Test
    fun `receives slot data`() = runBlocking{
        //Life data is lazy, so you have to observe it, elsewise it won´t change
        repo.getActiveSlots().observeForever {  }

        //enter valid slot code (server and db background stuff happens in fake now)
        repo.newCodeEntered(InternetAndDatabaseFake.VALID_CODE)

        //check if repo actualized live data with new client Info received from server
        assertTrue(repo.getActiveSlots().value !== null)
        assertTrue(repo.getActiveSlots().value!!.isNotEmpty())
        assertEquals(InternetAndDatabaseFake.VALID_CODE, repo.getActiveSlots().value!!.first().slotCode)
    }

    /**
     * check if repo maintains connection to server after valid slot code entry
     *
     */
    @Test
    fun `valid wait code entered - server connection maintained`() = runBlocking{
        //in the beginning there must not be an established connection
        assertFalse(repo.isConnectedToServer())
        repo.newCodeEntered(InternetAndDatabaseFake.VALID_CODE)
        //now there must be an established connection
        assertTrue(repo.isConnectedToServer())
        verify { webDBFake.initCommunication() }
    }

    /**
     * check if repo ends server connection when there are no more waiting slots
     *
     */
    @Test
    fun `waiting slot expires - repo shuts down server connection`() = runBlocking{
        repo.newCodeEntered(InternetAndDatabaseFake.VALID_CODE)
        //tell background fake to make slot expire
        webDBFake.expireAllSlots()
        //after last active slot expired repo kills connection
        assertFalse(repo.isConnectedToServer())
        verify { webDBFake.endCommunication() }
    }
    /**
     * Entering an invalid slot code leads to an invalid code error
     */
    @Test
    fun `invalid code - invalid code error`() = runBlocking{
        //Life data is lazy, so you have to observe it, elsewise it won´t change
        repo.getErrorNotifications().observeForever {  }

        repo.newCodeEntered(InternetAndDatabaseFake.INVALID_CODE)

        assertEquals(ClientErrors.INVALID_SLOT_CODE, repo.getErrorNotifications().value!!.last())
    }

    /**
     * NETWORK_ERROR, COULD_NOT_CONNECT, SERVER_DID_NOT_RESPOND are three possible
     * errors from the remote data source client handler. All three should make the
     * client repo pushing an INTERNET_ERROR to the getErrorNotifications() live Data.
     * This method checks if the error is pushed for NETWORK_ERROR
     */
    @Test
    fun `NETWORK_ERROR - INTERNET_ERROR`() = runBlocking{
        repo.newCodeEntered(InternetAndDatabaseFake.NETWORK_ERROR_CODE)
        assertEquals(ClientErrors.INTERNET_ERROR, repo.getErrorNotifications().value!!.last())
    }

    /**
     * NETWORK_ERROR, COULD_NOT_CONNECT, SERVER_DID_NOT_RESPOND are three possible
     * errors from the remote data source client handler. All three should make the
     * client repo pushing an INTERNET_ERROR to the getErrorNotifications() live Data.
     * This method checks if the error is pushed for NETWORK_ERROR
     */
    @Test
    fun `COULD_NOT_CONNECT - INTERNET_ERROR`() = runBlocking{
        repo.newCodeEntered(InternetAndDatabaseFake.COULD_NOT_CONNECT_CODE)
        assertEquals(ClientErrors.INTERNET_ERROR, repo.getErrorNotifications().value!!.last())
    }

    /**
     * NETWORK_ERROR, COULD_NOT_CONNECT, SERVER_DID_NOT_RESPOND are three possible
     * errors from the remote data source client handler. All three should make the
     * client repo pushing an INTERNET_ERROR to the getErrorNotifications() live Data.
     * This method checks if the error is pushed for NETWORK_ERROR
     */
    @Test
    fun `SERVER_DID_NOT_RESPOND - INTERNET_ERROR`() = runBlocking{
        repo.newCodeEntered(InternetAndDatabaseFake.SERVER_DID_NOT_RESPOND_CODE)
        assertEquals(ClientErrors.INTERNET_ERROR, repo.getErrorNotifications().value!!.last())
    }

    /**
     * If the internet connection is lost, refreshWaitingTime() must not
     * send a refresh request as usual, but instead has to re-establish a
     * connection to the server
     */
    @Test
    fun `refresh waiting time makes a reconnects when internet connection is lost`() = runBlocking{
        //first enter valid code
        repo.newCodeEntered(InternetAndDatabaseFake.VALID_CODE)
        //now we cause the Fake to simulate internet connection error
        repo.newCodeEntered(InternetAndDatabaseFake.NETWORK_ERROR_CODE)
        //now we try to refresh and expect the behaviour in the method description
        repo.refreshWaitingTime(InternetAndDatabaseFake.VALID_CODE)

        //Expected behaviour:
        verify { webDBFake.initCommunication() }
        verify { webDBFake.newCodeEntered(InternetAndDatabaseFake.VALID_CODE) }
        verify(exactly = 0){ webDBFake.refreshWaitingTime(any())}
    }

    /**
     * For "historical reasons" (in the beginning of the implementation phase)
     * the slot code parameter in the newCodeEntered Method is nullable,
     * This method just checks that calling the method with null leads to an invalid slot
     * code error.
     */
    @Test
    fun `calling newCodeEntered(null) - invalid slot code error`() = runBlocking{
        //Life data is lazy, so you have to observe it, elsewise it won´t change
        repo.getErrorNotifications().observeForever {  }
        //entering null
        repo.newCodeEntered(null)
        //check if error is there
        assertEquals(ClientErrors.INVALID_SLOT_CODE, repo.getErrorNotifications().value!!.last())
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

        companion object{
            /**
             * Valid slot code that leads to live data change
             */
            const val VALID_CODE = "V"

            /**
             * invalid slot code --> error message
             */
            const val INVALID_CODE = "I"
            /**
             * this slot code will "cause" an COULD_NOT_CONNECT error
             */
            const val COULD_NOT_CONNECT_CODE = "CNC"
            /**
             * this slot code will "cause" an SERVER_DID_NOT_RESPOND error
             */
            const val SERVER_DID_NOT_RESPOND_CODE = "SDNR"
            /**
             * this slot code will "cause" an SERVER_DID_NOT_RESPOND error
             */
            const val NETWORK_ERROR_CODE = "N"
        }

        /**
         * makes all slots expireing (as if the server would send endSlot@C
         *
         */
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

        /**
         * reacts depending on the slot code in the companion object.
         * elsewise behaviour not specified
         *
         * @param slotCode see slot codes in companion object
         */
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
                INVALID_CODE
                -> errorMessages.value = errorMessages.value !!+ ClientServerErrors.INVALID_SLOT_CODE
                COULD_NOT_CONNECT_CODE
                -> errorMessages.value = errorMessages.value !!+ ClientServerErrors.COULD_NOT_CONNECT
                SERVER_DID_NOT_RESPOND_CODE
                -> errorMessages.value = errorMessages.value !!+ ClientServerErrors.SERVER_DID_NOT_RESPOND
                NETWORK_ERROR_CODE
                -> errorMessages.value = errorMessages.value !!+ ClientServerErrors.SERVER_DID_NOT_RESPOND
            }
        }

        override fun refreshWaitingTime(slotCode: String) {
            if (!clientInfoList.value.isNullOrEmpty()){
                if (clientInfoList.value!!.first().slotCode == slotCode){
                    clientInfoList.value = clientInfoList.value!!.minus(clientInfoList.value!!.first())
                    clientInfoList.value = clientInfoList.value!!.plus(                    ClientInfo(
                        slotCode,
                        "Test",
                        DateTime(),
                        DateTime(),
                        Duration(0),
                        Duration(0)
                    ))
                }
            }
        }

        private val errorMessages = MutableLiveData<List<ClientServerErrors>>(listOf())
        override fun getErrors() = errorMessages as LiveData<List<ClientServerErrors>>

        private val clientInfoList = MutableLiveData<List<ClientInfo>>(listOf())
        override fun getAllClientInfoObservable() = clientInfoList as LiveData<List<ClientInfo>>

        override fun insert(clientInfo: ClientInfo) = throw NotImplementedError()
        override fun update(clientInfo: ClientInfo) = throw NotImplementedError()
        override fun getClientInfo(waitCode: String): ClientInfo? = throw NotImplementedError()
        override fun deleteClientInfo(info: ClientInfo) = throw NotImplementedError()
        override fun getClientInfoObservable(slotCode: String): LiveData<ClientInfo?> = throw NotImplementedError()
        override fun getAllClientInfo(): List<ClientInfo> = throw NotImplementedError()
        override fun clearTable() = throw NotImplementedError()

    }
}
