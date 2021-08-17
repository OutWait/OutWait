package edu.kit.outwait.client

import android.util.Log
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
import edu.kit.outwait.clientRepository.ClientRepository
import edu.kit.outwait.util.*
import edu.kit.outwait.utils.EspressoIdlingResource
import edu.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import edu.kit.outwait.waitingQueue.timeSlotModel.FixedTimeSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.Duration
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertNotEquals
import javax.inject.Inject

@HiltAndroidTest
class ManualUpdateTest {
    private lateinit var validSlotCodeToEnter: String
    private val firstSlotTime = DateTime().plusHours(1)

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Inject
    lateinit var instituteRepo: InstituteRepository

    @Inject
    lateinit var clientRepo: ClientRepository

    @Inject
    lateinit var clientDBDao: ClientInfoDao

    @Before
    fun init() {
        hiltRule.inject()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)

        establishPreconditions()
    }

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
        // generate valid slot code
        instituteRepo.newFixedSlot(
            DEFAULT_AUX_IDENTIFIER,
            firstSlotTime,
            Duration(DEFAULT_DURATION_MILLIS)
        )
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
        // logout of management
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.logout()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        // check that we are logged out and in the login fragment
        assert(!instituteRepo.isLoggedIn().value!!)

        onView(ViewMatchers.withId(R.id.etSlotCode)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
    }

    @After
    fun cleanUp() {
        // clean client DB (so view can navigate back to login fragment)
        clientDBDao.clearTable()

        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.logout()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()
    }

    // T23
    @Test
    fun manualUpdate() {
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
        // Move slot with management
        instituteRepo.login(VALID_TEST_USERNAME, VALID_TEST_PASSWORD)
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        val slot = (instituteRepo.getObservableTimeSlotList().value!!
            .filterIsInstance<FixedTimeSlot>().first())
        val newTime = slot.appointmentTime + Duration(60000) // Add a minute

        instituteRepo.changeFixedSlotInfo(
            slot.slotCode,
            slot.interval.toDuration(),
            slot.auxiliaryIdentifier,
            newTime
        )
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        // Refresh code
        val newReceivedTime = clientRepo.getActiveSlots().value!!.first().approximatedTime
        // Check if codes differ
        assertNotEquals(firstSlotTime, newReceivedTime)
    }
}
