package edu.kit.outwait.client

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import edu.kit.outwait.MainActivity
import edu.kit.outwait.R
import edu.kit.outwait.clientDatabase.ClientInfoDao
import edu.kit.outwait.instituteRepository.InstituteRepository
import edu.kit.outwait.util.*
import edu.kit.outwait.utils.EspressoIdlingResource
import edu.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.Duration
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class SlotCodeInputTest {
    private lateinit var invalidSlotCodeToEnter: String
    private lateinit var validSlotCodeToEnter: String

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Inject
    lateinit var instituteRepo: InstituteRepository

    @Inject
    lateinit var clientDBDao: ClientInfoDao

    /**
     * Advise hilt to inject the needed dependencies, register idling resources and
     * establish the specified preconditions
     */
    @Before
    fun init() {
        hiltRule.inject()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        establishPreconditions()
    }

    /**
     * Establish the preconditions as stated in the global test definitions for
     * T7 and T8
     */
    private fun establishPreconditions() {
        // perform login
        instituteRepo.login(VALID_TEST_USERNAME, VALID_TEST_PASSWORD)
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        // check that we are logged in
        assert(instituteRepo.isLoggedIn().value!!)
        // ensure that waiting queue is empty to begin with (on the server side)
        val timeSlots = instituteRepo.getObservableTimeSlotList().value

        if (timeSlots != null && timeSlots.isNotEmpty()) {
            val onlyClientSlots: List<ClientTimeSlot> = timeSlots.filterIsInstance<ClientTimeSlot>()
            for (ClientTimeSlot in onlyClientSlots) {
                // delete slot with retrieved slotCode from waiting queue
                instituteRepo.deleteSlot(ClientTimeSlot.slotCode)
                Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
            }
            // save the transaction and the changes made (execute on main thread)
            CoroutineScope(Dispatchers.Main).launch {
                instituteRepo.saveTransaction()
            }
            Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        }
        // generate valid and invalid slot code
        instituteRepo.newSpontaneousSlot(DEFAULT_AUX_IDENTIFIER, Duration(DEFAULT_DURATION_MILLIS))
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        // save the transaction (execute on main thread)
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.saveTransaction()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        // assert that exactly one (valid) slot is in queue and its respective slotCode in instituteDB
        assert(instituteRepo.getObservableTimeSlotList().value != null)
        val allClientSlots = instituteRepo.getObservableTimeSlotList().value!!
            .filterIsInstance<ClientTimeSlot>()
        assert(allClientSlots.size == 1)
        // retrieve the (valid) slotCode
        validSlotCodeToEnter = allClientSlots.first().slotCode
        // generate invalid slotCode by reversing the only (valid) slotCode
        invalidSlotCodeToEnter = StringBuilder(validSlotCodeToEnter).reverse().toString()
        // logout of management
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.logout()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        // check that we are logged out and in the login fragment
        assertFalse(instituteRepo.isLoggedIn().value!!)

        onView(ViewMatchers.withId(R.id.etSlotCode)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
    }

    /**
     * Unregister idling resources and
     * close the activity
     */
    @After
    fun cleanUp() {
        // clean client DB (so view can navigate back to login fragment)
        clientDBDao.clearTable()

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()
    }

    /**
     * Tests T8, assuming the preconditions are met.
     * The actions in themselves are conditions to be verified
     */
    @Test
    fun invalidSlotCodeInput() {
        onView(ViewMatchers.withId(R.id.etSlotCode))
            .perform(
                TextSetter.setTextEditText(invalidSlotCodeToEnter),
                ViewActions.closeSoftKeyboard()
            )
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        //check that code input was not successful
        onView(ViewMatchers.withId(R.id.btnLoginFrag)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
    }

    /**
     * Tests T7 and T20, assuming the preconditions are met.
     * The actions in themselves are conditions to be verified.
     * The "client view" is the same as the "remainingTimeFragment" hence both global test cases are
     * tested.
     */
    @Test
    fun validSlotCodeInput() {
        onView(ViewMatchers.withId(R.id.etSlotCode))
            .perform(
                TextSetter.setTextEditText(validSlotCodeToEnter),
                ViewActions.closeSoftKeyboard()
            )
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // check if we navigated to remainingTimeFragment
        onView(ViewMatchers.withId(R.id.btnRefresh)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
    }
}
