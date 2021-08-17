package edu.kit.outwait.management

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import edu.kit.outwait.MainActivity
import edu.kit.outwait.R
import edu.kit.outwait.customDataTypes.Mode
import edu.kit.outwait.customDataTypes.Preferences
import edu.kit.outwait.dataItem.TimeSlotItem
import edu.kit.outwait.instituteRepository.InstituteRepository
import edu.kit.outwait.recyclerviewSetUp.viewHolder.BaseViewHolder
import edu.kit.outwait.util.*
import edu.kit.outwait.utils.EspressoIdlingResource
import edu.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.Duration
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class ChangeToModeOneTest {
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

    /**
     * Establish the preconditions as stated in the global test definitions
     *
     */
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
        // set default preferences (with default durations) and ensure that mode 2 is active
        val preconditionPrefs = Preferences(
            Duration(DEFAULT_DURATION_MILLIS), Duration(DEFAULT_DURATION_MILLIS),
            Duration(DEFAULT_DURATION_MILLIS), Duration(DEFAULT_DURATION_MILLIS), Mode.TWO
        )

        instituteRepo.changePreferences(preconditionPrefs)
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        // we are logged in and in the management view with preferences set with mode 2 active
        // all specified preconditions are met
    }

    /**
     * Logout of management, unregister idling resources and
     * close the activity
     */
    @After
    fun cleanUp() {

        // logout of management
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.logout()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()
    }

    /**
     * Tests T29, assuming the preconditions are met.
     * The actions in themselves are conditions to be verified
     */
    @Test
    fun changeToModeOne() {
        // perform action 1 (open settings)
        onView(withId(R.id.config)).perform(click())

        // perform action 2 (set active mode to mode 1)
        onView(withId(R.id.sMode)).perform(click())
        onView(withId(R.id.tvSwitchText)).check(matches(withText(StringResource.getResourceString(R.string.modeOne))))

        // perform action 3 (save the settings)
        onView(withId(R.id.btnSave)).perform(scrollTo(), click())

        // navigate back to the waiting queue
        onView(isRoot()).perform((pressBack()))

        // perform action 4
        onView(withId(R.id.floatingActionButton)).perform(click())

        // check if only spontaneous slots are possible to add
        onView(withId(R.id.cbIsFixedSlot)).check(
            matches(withEffectiveVisibility(Visibility.INVISIBLE))
        )
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        onView(withId(R.id.ivSaveTransaction)).perform(click())

        // check if allocated slot is immediately next (therefore has no appointment time)
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT_POSITION,
                click()
            )
        )
        onView(withId(R.id.tvTitleAppointment)).check(
            matches(withEffectiveVisibility(Visibility.GONE))
        )
    }
}
