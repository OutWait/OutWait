package edu.kit.outwait.client

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import edu.kit.outwait.MainActivity
import edu.kit.outwait.NotificationManagerModule
import edu.kit.outwait.R
import edu.kit.outwait.clientDatabase.ClientInfoDao
import edu.kit.outwait.customDataTypes.Mode
import edu.kit.outwait.customDataTypes.Preferences
import edu.kit.outwait.dataItem.TimeSlotItem
import edu.kit.outwait.instituteRepository.InstituteRepository
import edu.kit.outwait.notifManager
import edu.kit.outwait.recyclerviewSetUp.viewHolder.BaseViewHolder
import edu.kit.outwait.services.NotifManager
import edu.kit.outwait.services.PENDING_NOTIFICATION_ID
import edu.kit.outwait.util.*
import edu.kit.outwait.utils.EspressoIdlingResource
import edu.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import io.mockk.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.Duration
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Singleton

@UninstallModules(NotificationManagerModule::class)
@HiltAndroidTest
class NotificationAfterSlotDelete {
    /**
     * Inject a new NotifManager (mocked with MockK, to make notify() calls
     * for delay notification and pending appointment notifications verifiable
     */
    @Module
    @InstallIn(SingletonComponent::class)
    object TestNotifManagerModule {
        /**
         * Returns a mocked NotifManager (wrapper for the android systems notification manager)
         * that does nothing when notify() is called
         *
         * @return mocked instance of NotifManager, the injected wrapper for NotificationManager
         */
        @notifManager
        @Provides
        @Singleton
        fun bindManagerMockk(): NotifManager {
            val mokka = mockk<NotifManager>()
            every { mokka.notify(any(), any()) } just runs
            return mokka
        }

    }

    private lateinit var thirdSlotCode: String
    private lateinit var secondSlotCode: String

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Inject
    lateinit var instituteRepo: InstituteRepository

    @Inject
    lateinit var clientDBDao: ClientInfoDao

    @notifManager
    @Inject
    lateinit var injectedManager: NotifManager

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
        // assert that we are in management fragment
        onView(withId(R.id.floatingActionButton)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
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
        // check that we are logged out
        assert(!instituteRepo.isLoggedIn().value!!)
        // clean client DB (so view can navigate back to login fragment)
        clientDBDao.clearTable()

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()
    }

    /**
     * Tests T16, assuming the preconditions are met.
     * The actions in themselves are conditions to be verified
     */
    @Test
    fun receiveSuddenPendingAppointment() {
        // perform action 1 (add first slot with 20min duration)
        onView(withId(R.id.floatingActionButton)).perform(ViewActions.click())
        // add auxiliary identifier
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(ViewActions.typeText(FIRST_SLOT_IDENTIFIER), ViewActions.closeSoftKeyboard())
        // set duration to 20min
        onView(withId(R.id.addSlotDuration)).perform(ViewActions.scrollTo())
        // clear the former value and enter new value
        DigitSelector.pressClear(R.id.addSlotDuration)
        DigitSelector.pressDigit(DigitSelector.digitTwo, R.id.addSlotDuration)
        DigitSelector.pressDigit(DigitSelector.digitZero, R.id.addSlotDuration)
        // complete slot allocation
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(ViewActions.click())
        //perform action 2 (add second slot with 20min duration)
        onView(withId(R.id.floatingActionButton)).perform(ViewActions.click())
        // add auxiliary identifier
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(ViewActions.typeText(SECOND_SLOT_IDENTIFIER), ViewActions.closeSoftKeyboard())
        // set duration to 20min
        onView(withId(R.id.addSlotDuration)).perform(ViewActions.scrollTo())
        // clear the former value and enter new value
        DigitSelector.pressClear(R.id.addSlotDuration)
        DigitSelector.pressDigit(DigitSelector.digitTwo, R.id.addSlotDuration)
        DigitSelector.pressDigit(DigitSelector.digitZero, R.id.addSlotDuration)
        // complete slot allocation
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(ViewActions.click())
        //perform action 3 (add third slot with 20min duration)
        onView(withId(R.id.floatingActionButton)).perform(ViewActions.click())
        // add auxiliary identifier
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(ViewActions.typeText(THIRD_SLOT_IDENTIFIER), ViewActions.closeSoftKeyboard())
        // set duration to 20min
        onView(withId(R.id.addSlotDuration)).perform(ViewActions.scrollTo())
        // clear the former value and enter new value
        DigitSelector.pressClear(R.id.addSlotDuration)
        DigitSelector.pressDigit(DigitSelector.digitTwo, R.id.addSlotDuration)
        DigitSelector.pressDigit(DigitSelector.digitZero, R.id.addSlotDuration)
        // complete slot allocation
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(ViewActions.click())
        // perform action 3.2 (save the transaction)
        onView(withId(R.id.ivSaveTransaction))
            .perform(ViewActions.click())
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
        // logout of management to get to loginFragment
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
        onView(withId(R.id.btnRefresh)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
        // login as management again to perform action 5
        instituteRepo.login(VALID_TEST_USERNAME, VALID_TEST_PASSWORD)
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        // check that we are logged in
        assert(instituteRepo.isLoggedIn().value!!)
        // perform action 5 (delete the second slot in queue)
        // (under the view/gui as we are still in remainingTimeFragment)
        instituteRepo.deleteSlot(secondSlotCode)
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        // perform action 6 (save the transaction and the changes made (execute on main thread))
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.saveTransaction()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        // check the result and assert expected result is met
        // notify for pending appointment notification was called on the mock
        verify(exactly = 1)
        { injectedManager.notify(PENDING_NOTIFICATION_ID, any()) }


    }
}
