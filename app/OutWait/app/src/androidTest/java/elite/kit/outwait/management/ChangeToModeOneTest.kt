package elite.kit.outwait.management

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
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
import elite.kit.outwait.util.StringResource
import elite.kit.outwait.util.VALID_TEST_PASSWORD
import elite.kit.outwait.util.VALID_TEST_USERNAME
import elite.kit.outwait.utils.EspressoIdlingResource
import elite.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import org.joda.time.Duration
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

private const val FIRST_SLOT_POSITION = 0

private const val WAIT_FOR_SERVER_RESPONSE = 3000L
private const val WAIT_FOR_UI_RESPONSE = 1000L
private const val DEFAULT_DURATION = 600000L

//@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ChangeToModeOneTest {


    private val preconditionPrefs = Preferences(Duration(DEFAULT_DURATION), Duration(DEFAULT_DURATION),
        Duration(DEFAULT_DURATION),Duration(DEFAULT_DURATION), Mode.TWO)


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
        Thread.sleep(WAIT_FOR_SERVER_RESPONSE)

        // check that we are logged in
        assert(instituteRepo.isLoggedIn().value!!)
        /*
        onView(withId(R.id.floatingActionButton)).check(
            matches(
                isDisplayed()
            )
        )
         */

        // ensure that waiting queue is empty to begin with
        val timeSlots = instituteRepo.getObservableTimeSlotList().value

        if (timeSlots != null && timeSlots.isNotEmpty()) {
            val onlyClientSlots : List<ClientTimeSlot> = timeSlots.filterIsInstance<ClientTimeSlot>()
            for (ClientTimeSlot in onlyClientSlots){
                    // delete slot with retrieved slotCode from waiting queue
                    instituteRepo.deleteSlot(ClientTimeSlot.slotCode)
                    Thread.sleep(WAIT_FOR_SERVER_RESPONSE)
                }
            // save the transaction and the changes made
            instituteRepo.saveTransaction()
        }

        // set default preferences (all durations to 10mins) and ensure that mode 2 is active
        instituteRepo.changePreferences(preconditionPrefs)
        Thread.sleep(WAIT_FOR_SERVER_RESPONSE)

        // we are logged in and in the management view with preferences set with mode 2 active
        // all specified preconditions are met
    }

    /*
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }
     */

    @After
    fun cleanUp(){
        // clean up waiting queue (on server side also)
        val timeSlots = instituteRepo.getObservableTimeSlotList().value


        if (timeSlots != null && timeSlots.isNotEmpty()) {
            val onlyClientSlots : List<ClientTimeSlot> = timeSlots.filterIsInstance<ClientTimeSlot>()
            for (ClientTimeSlot in onlyClientSlots){
                // delete slot with retrieved slotCode from waiting queue
                instituteRepo.deleteSlot(ClientTimeSlot.slotCode)
                Thread.sleep(WAIT_FOR_SERVER_RESPONSE)
            }
            // save the transaction and the changes made
            instituteRepo.saveTransaction()
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
        // TODO Magic String, woher holen? XML Resources
        onView(withId(R.id.tvSwitchText)).check(matches(withText("Mode 1")))

        // perform action 3 (save the settings)
        onView(withId(R.id.btnSave)).perform(click())
        Thread.sleep(WAIT_FOR_SERVER_RESPONSE)
        // navigate back to the waiting queue
        onView(isRoot()).perform((pressBack()))

        // perform action 4
        onView(withId(R.id.floatingActionButton)).perform(click())
        Thread.sleep(WAIT_FOR_UI_RESPONSE)

        // check if only spontaneous slots are possible to add
        // TODO Klappt das so?
        onView(withId(R.id.cbIsFixedSlot)).check(
            matches(withEffectiveVisibility(Visibility.INVISIBLE))
        )
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        Thread.sleep(WAIT_FOR_SERVER_RESPONSE)
        onView(withId(R.id.ivSaveTransaction)).perform(click())
        Thread.sleep(WAIT_FOR_SERVER_RESPONSE)

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
