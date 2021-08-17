package edu.kit.outwait.instituteRepository

import edu.kit.outwait.customDataTypes.ReceivedList
import edu.kit.outwait.customDataTypes.ReceivedListUtil
import edu.kit.outwait.customDataTypes.SpontaneousSlot
import edu.kit.outwait.instituteDatabase.facade.InstituteDBFacade
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.DateTime
import org.joda.time.Duration
import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests the aux helper class with help of the DataBaseFake class
 *
 */
class AuxHelperTest {

    private lateinit var auxHelper : AuxHelper

    @Before
    fun setUp() {
        auxHelper = AuxHelper(DataBaseFake())
    }

    /**
     * When the aux helper receives a list with one new slot, it has to match the latest
     * auxiliary identifier to it
     *
     */
    @Test
    fun `new aux is assigned to new received slot in transaction`()= runBlockingTest {
        auxHelper.newAux("Mr. Mustermann, vacunation")
        val auxIds = auxHelper.receivedList(ReceivedListUtil.prepareReceivedList(1), true)
        assertEquals("Mr. Mustermann, vacunation", auxIds["SlotCode1"])
    }

    /**
     * When the aux helper receives a received list and no new aux was entered before,
     * all slots get an empty auxiliary identifier by default
     * (this happens e.g. after login)
     *
     */
    @Test
    fun `receive slot list after login - empty identifiers are assigned to received slots`()= runBlockingTest {
        val auxIds = auxHelper.receivedList(ReceivedListUtil.prepareReceivedList(2), true)
        assertEquals("", auxIds["SlotCode1"])
        assertEquals("", auxIds["SlotCode2"])
    }

    /**
     * Checks that the aux helper does not delete auxiliary identifiers during a transaction.
     * This would lead to the loss of an aux identifier if the transaction gets aborted
     *
     */
    @Test
    fun `obsolete identifier is NOT deleted during transaction is running`()= runBlockingTest {
        auxHelper.receivedList(ReceivedListUtil.prepareReceivedList(1), true)
        auxHelper.newAux("Hans Jürgen")
        auxHelper.receivedList(ReceivedListUtil.prepareReceivedList(2), true)
        val auxIds = auxHelper.receivedList(ReceivedListUtil.prepareReceivedList(1), true)
        /*
        Slot 2 is not anymore in the received List, but because we are in a transaction the aux
        identifier must not be deleted
         */
        assertEquals("Hans Jürgen", auxIds["SlotCode2"])
    }

    /**
     * Checks if our own "auxiliary identifier garbage collector" works and removes the
     * aux identifiers of slots which are definitely deleted
     *
     */
    @Test
    fun `obsolete identifier is deleted AFTER TRANSACTION`()= runBlockingTest {
        auxHelper.receivedList(ReceivedListUtil.prepareReceivedList(1), true)
        auxHelper.newAux("Hans Jürgen")
        auxHelper.receivedList(ReceivedListUtil.prepareReceivedList(2), true)
        val auxIds = auxHelper.receivedList(ReceivedListUtil.prepareReceivedList(1), false)
        /*
        Slot 2 is not anymore in the received List, and because we are not in a transaction the aux
        identifier must be deleted
         */
        assertEquals(null, auxIds["SlotCode2"])
        assertEquals(1, auxIds.size)
    }

    /**
     * Tests if an aux identifier can be changes successfully
     *
     */
    @Test
    fun `auxiliary identifier can be changed`()= runBlockingTest {
        auxHelper.newAux("Mr. Mustermann, vacunation")
        auxHelper.receivedList(ReceivedListUtil.prepareReceivedList(1), true)
        auxHelper.changeAux("SlotCode1", "Mr. Mustermann, vac.2x")
        val auxIds = auxHelper.receivedList(ReceivedListUtil.prepareReceivedList(1), true)
            assertEquals("Mr. Mustermann, vac.2x", auxIds["SlotCode1"])
    }
}
