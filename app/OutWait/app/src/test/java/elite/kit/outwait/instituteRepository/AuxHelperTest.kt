package elite.kit.outwait.instituteRepository

import elite.kit.outwait.customDataTypes.ReceivedList
import elite.kit.outwait.customDataTypes.SpontaneousSlot
import elite.kit.outwait.instituteDatabase.facade.InstituteDBFacade
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.DateTime
import org.joda.time.Duration
import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Test

class AuxHelperTest {

    private lateinit var auxHelper : AuxHelper

    @Before
    fun setUp() {
        auxHelper = AuxHelper(DataBaseStub())
    }

    @After
    fun tearDown() {

    }

    /**
     * Creates a received list with Slots with the Slot Codes
     * SlotCode1, SlotCode2, SlotCode3...
     *
     * @param nrOfSlots number of slots the list shall contain
     */
    private fun prepareReceivedList(nrOfSlots: Int): ReceivedList{
        val slotCodes = mutableListOf<String>()
        val slots = mutableListOf<SpontaneousSlot>()
        for (i in 1..nrOfSlots){
            val code = "SlotCode$i"
            slotCodes += code
            slots += SpontaneousSlot(Duration(0), code)
        }
        return ReceivedList(
            DateTime(),
            slotCodes,
            slots,
            listOf()
        )
    }


    @Test
    fun `new aux is assigned to new received slot in transaction`()= runBlockingTest {
        auxHelper.newAux("Mr. Mustermann, vacunation")
        val auxIds = auxHelper.receivedList(prepareReceivedList(1), true)
        assertEquals("Mr. Mustermann, vacunation", auxIds["SlotCode1"])
    }

    @Test
    fun `receive slot list after login - empty identifiers are assigned to received slots`()= runBlockingTest {
        val auxIds = auxHelper.receivedList(prepareReceivedList(2), true)
        assertEquals("", auxIds["SlotCode1"])
        assertEquals("", auxIds["SlotCode2"])
    }

    @Test
    fun `obsolete identifier is NOT deleted during transaction is running`()= runBlockingTest {
        auxHelper.receivedList(prepareReceivedList(1), true)
        auxHelper.newAux("Hans Jürgen")
        auxHelper.receivedList(prepareReceivedList(2), true)
        val auxIds = auxHelper.receivedList(prepareReceivedList(1), true)
        /*
        Slot 2 is not anymore in the received List, but because we are in a transaction the aux
        identifier must not be deleted
         */
        assertEquals("Hans Jürgen", auxIds["SlotCode2"])
    }

    @Test
    fun `obsolete identifier is deleted AFTER TRANSACTION`()= runBlockingTest {
        auxHelper.receivedList(prepareReceivedList(1), true)
        auxHelper.newAux("Hans Jürgen")
        auxHelper.receivedList(prepareReceivedList(2), true)
        val auxIds = auxHelper.receivedList(prepareReceivedList(1), false)
        /*
        Slot 2 is not anymore in the received List, and because we are not in a transaction the aux
        identifier must be deleted
         */
        assertEquals(null, auxIds["SlotCode2"])
        assertEquals(1, auxIds.size)
    }

    @Test
    fun `auxiliary identifier can be changed`()= runBlockingTest {
        auxHelper.newAux("Mr. Mustermann, vacunation")
        auxHelper.receivedList(prepareReceivedList(1), true)
        auxHelper.changeAux("SlotCode1", "Mr. Mustermann, vac.2x")
        val auxIds = auxHelper.receivedList(prepareReceivedList(1), true)
            assertEquals("Mr. Mustermann, vac.2x", auxIds["SlotCode1"])
    }

    /**
     * Simulates the Behavior of the real database, only without persistance
     *
     */
    private class DataBaseStub : InstituteDBFacade{

        private val auxMap: MutableMap<String, String> = HashMap<String, String>()

        override suspend fun insertUpdateAux(slotCode: String, aux: String) {
            auxMap[slotCode] = aux
        }

        override suspend fun getAuxiliaryIdentifiers(): Map<String, String> {
            return auxMap.toMap()
        }

        override suspend fun deleteAux(slotCode: String) {
            auxMap.remove(slotCode)
        }

        override suspend fun deleteAll() {
            auxMap.clear()
        }

        override suspend fun insertUpdateLoginData(username: String, password: String) {
            throw NotImplementedError()
        }

        override suspend fun getUserName(): String {
            throw NotImplementedError()
        }

        override suspend fun getPassword(): String {
            throw NotImplementedError()
        }

        override suspend fun loginDataSaved(): Boolean {
            throw NotImplementedError()
        }
    }
}
