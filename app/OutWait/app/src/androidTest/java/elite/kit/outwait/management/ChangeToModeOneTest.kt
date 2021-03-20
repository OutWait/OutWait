package elite.kit.outwait.management

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import elite.kit.outwait.MainActivity
import elite.kit.outwait.R
import elite.kit.outwait.customDataTypes.Mode
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.dataItem.TimeSlotItem
import elite.kit.outwait.instituteRepository.InstituteRepository
import elite.kit.outwait.recyclerviewSetUp.viewHolder.BaseViewHolder
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

        // set default preferences (with default durations) and ensure that mode 2 is active
        val preconditionPrefs = Preferences(Duration(DEFAULT_DURATION), Duration(DEFAULT_DURATION),
            Duration(DEFAULT_DURATION),Duration(DEFAULT_DURATION), Mode.TWO)

        instituteRepo.changePreferences(preconditionPrefs)
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // we are logged in and in the management view with preferences set with mode 2 active
        // all specified preconditions are met
    }


    @After
    fun cleanUp() {
        // clean up waiting queue (on server side also)
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

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()
    }

    // tests T29
    @Test
    fun changeToModeOne() {

        // perform action 1 (open settings)
        onView(withId(R.id.config)).perform(click())
        Thread.sleep(WAIT_FOR_UI_RESPONSE)

        // perform action 2 (set active mode to mode 1)
        onView(withId(R.id.sMode)).perform(click())
        Thread.sleep(WAIT_FOR_UI_RESPONSE)
        onView(withId(R.id.tvSwitchText)).check(matches(withText(StringResource.getResourceString(R.string.modeOne))))

        // perform action 3 (save the settings)
        onView(withId(R.id.btnSave)).perform(scrollTo(), click())
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        // navigate back to the waiting queue
        onView(isRoot()).perform((pressBack()))

        // perform action 4
        onView(withId(R.id.floatingActionButton)).perform(click())
        Thread.sleep(WAIT_FOR_UI_RESPONSE)

        // check if only spontaneous slots are possible to add
        onView(withId(R.id.cbIsFixedSlot)).check(
            matches(withEffectiveVisibility(Visibility.INVISIBLE))
        )
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        onView(withId(R.id.ivSaveTransaction)).perform(click())
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

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
