package elite.kit.outwait.client

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
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

@HiltAndroidTest
class NotificationAfterSlotDelete {

    private lateinit var thirdSlotCode: String
    private lateinit var secondSlotCode: String

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
            val onlyClientSlots: List<ClientTimeSlot> =
                timeSlots.filterIsInstance<ClientTimeSlot>()
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
        // set preferences according to precondition
        // (notification time 20min and mode 1 is active)
        val preconditionPrefs = Preferences(
            Duration(DEFAULT_DURATION_MILLIS), Duration(TWENTY_MINUTE_DURATION_MILLIS),
            Duration(DEFAULT_DURATION_MILLIS), Duration(DEFAULT_DURATION_MILLIS), Mode.ONE
        )
        instituteRepo.changePreferences(preconditionPrefs)
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
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

    // tests T17
    @Test
    fun receiveSuddenPendingAppointment() {

        // assert that we are in management fragment
        onView(withId(R.id.floatingActionButton)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )

        // perform action 1 (add first slot with 20min duration)
        // TODO Numpad über GUI?
        instituteRepo.newSpontaneousSlot(FIRST_SLOT_IDENTIFIER, Duration(
            TWENTY_MINUTE_DURATION_MILLIS))
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        //perform action 2 (add second slot with 20min duration)
        //TODO Über GUI mit Numpad EIngabe?
        instituteRepo.newSpontaneousSlot(SECOND_SLOT_IDENTIFIER, Duration(TWENTY_MINUTE_DURATION_MILLIS))
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        //perform action 3 (add third slot with 20min duration)
        //TODO Über GUI mit Numpad EIngabe?
        instituteRepo.newSpontaneousSlot(THIRD_SLOT_IDENTIFIER, Duration(TWENTY_MINUTE_DURATION_MILLIS))
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // perform action 3.2 (save the transaction) // TODO fehlt in TestDef!
        onView(withId(R.id.ivSaveTransaction))
            .perform(ViewActions.click())
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // retrieve the slotCode of the second slot in queue
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                SECOND_SLOT_POSITION,
                ViewActions.click()
            )
        )
        secondSlotCode = ReadText.getText(onView(withId(R.id.tvSlotCodeDetail)))
        // close slot detail dialog
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(ViewActions.click())

        // retrieve the slotCode of the third slot in queue
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                THIRD_SLOT_POSITION,
                ViewActions.click()
            )
        )
        thirdSlotCode = ReadText.getText(onView(withId(R.id.tvSlotCodeDetail)))
        // close slot detail dialog
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(ViewActions.click())

        // perform action 3.3 (logout of management to get to loginFragment) //TODO missing in TestDef.
        // logout of management
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.logout()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // check that we are logged out
        assert(!instituteRepo.isLoggedIn().value!!)

        // perform action 4 (enter third slotCode as client)
        onView(withId(R.id.etSlotCode))
            .perform(TextSetter.setTextEditText(thirdSlotCode), ViewActions.closeSoftKeyboard())
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // check if we navigated to remainingTimeFragment
        Espresso.onView(ViewMatchers.withId(R.id.btnRefresh)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )

        // login as management again to perform action 5
        instituteRepo.login(
            VALID_TEST_USERNAME,
            VALID_TEST_PASSWORD
        )
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // check that we are logged in
        assert(instituteRepo.isLoggedIn().value!!)

        // perform action 5
        instituteRepo.deleteSlot(secondSlotCode)
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // save the transaction and the changes made (execute on main thread)
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.saveTransaction()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        //TODO Check for displayed pending appointment push notification using NotifManager Mock

    }
}
