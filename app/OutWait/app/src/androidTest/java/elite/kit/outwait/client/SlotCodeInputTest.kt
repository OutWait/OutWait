package elite.kit.outwait.client

import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import elite.kit.outwait.MainActivity
import elite.kit.outwait.R
import elite.kit.outwait.instituteRepository.InstituteRepository
import elite.kit.outwait.util.*
import elite.kit.outwait.utils.EspressoIdlingResource
import elite.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.Duration
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

//@RunWith(AndroidJUnit4::class)
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


    @Before
    fun init() {
        hiltRule.inject()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        establishPreconditions()
    }

    private fun establishPreconditions() {

        // perform login
        instituteRepo.login(
            VALID_TEST_USERNAME,
            VALID_TEST_PASSWORD
        )
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // check that we are logged in
        assert(instituteRepo.isLoggedIn().value!!)

        // ensure that waiting queue is empty to begin with
        val timeSlots = instituteRepo.getObservableTimeSlotList().value

        if (timeSlots != null && timeSlots.isNotEmpty()) {
            val onlyClientSlots : List<ClientTimeSlot> = timeSlots.filterIsInstance<ClientTimeSlot>()
            for (ClientTimeSlot in onlyClientSlots){
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
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.saveTransaction()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // assert that exactly one (valid) slot is in queue and its respective slotCode in instituteDB
        assert(instituteRepo.getObservableTimeSlotList().value != null)
        val allClientSlots = instituteRepo.getObservableTimeSlotList().value!!
            .filterIsInstance<ClientTimeSlot>()
        assert(allClientSlots.size == 1)

        // retrieve (valid) slotCode
        validSlotCodeToEnter = allClientSlots.first().slotCode

        // generate (invalid) slotCode by reversing the only valid slotCode
        invalidSlotCodeToEnter = StringBuilder(validSlotCodeToEnter).reverse().toString()
        Log.i("slotcode","$invalidSlotCodeToEnter+++$validSlotCodeToEnter")

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
        // perform login
        instituteRepo.login(
            VALID_TEST_USERNAME,
            VALID_TEST_PASSWORD
        )
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // check that we are logged in
        assert(instituteRepo.isLoggedIn().value!!)

        // clean up waiting queue (on server side also)
        val timeSlots = instituteRepo.getObservableTimeSlotList().value


        if (timeSlots != null && timeSlots.isNotEmpty()) {
            val onlyClientSlots : List<ClientTimeSlot> = timeSlots.filterIsInstance<ClientTimeSlot>()
            for (ClientTimeSlot in onlyClientSlots){
                // delete slot with retrieved slotCode from waiting queue
                instituteRepo.deleteSlot(ClientTimeSlot.slotCode)
                Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
            }
            // save the transaction and the changes made
            CoroutineScope(Dispatchers.Main).launch {
                instituteRepo.saveTransaction()
            }
            Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        }
        // logout of management
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.logout()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // check that we are logged out
        assert(!instituteRepo.isLoggedIn().value!!)

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()
    }


    // tests T8
    @Test
    fun invalidSlotCodeInput() {
        onView(ViewMatchers.withId(R.id.etSlotCode))
            .perform(TextSetter.setTextEditText(invalidSlotCodeToEnter), ViewActions.closeSoftKeyboard())
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        //check for displayed toast with "invalid code" error message
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.INVALID_SLOT_CODE))).inRoot(ToastMatcher()).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed())
        )
    }


    // tests T7 and T20 (the "client view" is the "remainingTimeFragment")
    @Test
    fun validSlotCodeInput() {
        onView(ViewMatchers.withId(R.id.etSlotCode))
            .perform(TextSetter.setTextEditText(validSlotCodeToEnter), ViewActions.closeSoftKeyboard())
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // check if we navigated to remainingTimeFragment
        onView(ViewMatchers.withId(R.id.btnRefresh)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
    }
}
