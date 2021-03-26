package elite.kit.outwait.client

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import elite.kit.outwait.*
import elite.kit.outwait.R
import elite.kit.outwait.customDataTypes.Mode
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.instituteRepository.InstituteRepository
import elite.kit.outwait.services.DELAY_NOTIFICATION_ID
import elite.kit.outwait.services.NotifManager
import elite.kit.outwait.util.*
import elite.kit.outwait.utils.EspressoIdlingResource
import elite.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import io.mockk.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.Duration
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import util.DigitSelector
import javax.inject.Inject
import javax.inject.Singleton

@UninstallModules(NotificationManagerModule::class)
@HiltAndroidTest
class NotificationAfterDelay {

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
         * @param app global application context
         * @return mocked instance of NotifManager, the injected wrapper for NotificationManager
         */
        @notifManager
        @Provides
        @Singleton
        fun bindManagerMockk(@ApplicationContext app: Context): NotifManager {
            val mokka = mockk<NotifManager>()
            every { mokka.notify(any(), any()) } just runs
            return mokka
        }

    }

    private lateinit var firstSlotCode: String
    private lateinit var secondSlotCode: String
    private lateinit var thirdSlotCode: String

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Inject
    lateinit var instituteRepo: InstituteRepository

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
        // ensure that waiting queue is empty (on server side) to begin with
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

        // ensure that we are logged in for the following tests
        assert(instituteRepo.isLoggedIn().value!!)
    }

    /**
     * Clean up the waiting queue on the serverside, unregister idling resources and
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


        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()
    }

    /**
     * Tests T15, assuming the preconditions are met.
     * The actions in themselves are conditions to be verified
     */
    @Test
    fun receiveDelayNotification() {

        // perform action 1 (open settings) and verify success
        onView(withId(R.id.config)).perform(ViewActions.click())
        //TODO Thread.sleep(WAIT_FOR_UI_RESPONSE)
        onView(withId(R.id.btnLogout)).check(matches(isDisplayed()))

        // perform action 2 (scroll to numpad and set 30min delay notification time)
        onView(withId(R.id.configDelayDuration)).perform(ViewActions.scrollTo())
        // clear the former value and enter new value
        DigitSelector.pressClear(R.id.configDelayDuration)
        DigitSelector.pressDigit( DigitSelector.digitThree, R.id.configDelayDuration)
        DigitSelector.pressDigit( DigitSelector.digitZero, R.id.configDelayDuration)

        //perform action 2.2 (save changed settings)
        onView(withId(R.id.btnSave)).perform(ViewActions.scrollTo(), click())

        // perform action 3 (navigate back to the waiting queue) and verify success
        onView(ViewMatchers.isRoot()).perform((ViewActions.pressBack()))
        onView(withId(R.id.floatingActionButton)).check(matches(isDisplayed()))

        // perform action 4 (add slot 1 with 10min duration)
        onView(withId(R.id.floatingActionButton)).perform(click())
        // add auxiliary identifier
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(ViewActions.typeText(FIRST_SLOT_IDENTIFIER), ViewActions.closeSoftKeyboard())
        // set duration to 10min
        onView(withId(R.id.addSlotDuration)).perform(ViewActions.scrollTo())
        // clear the former value and enter new value
        DigitSelector.pressClear(R.id.addSlotDuration)
        DigitSelector.pressDigit( DigitSelector.digitOne, R.id.addSlotDuration)
        DigitSelector.pressDigit( DigitSelector.digitZero, R.id.addSlotDuration)
        // complete slot allocation
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())

        // perform action 2 (add Slot2 with 20min duration)
        onView(withId(R.id.floatingActionButton)).perform(click())
        // add auxiliary identifier
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(ViewActions.typeText(SECOND_SLOT_IDENTIFIER), ViewActions.closeSoftKeyboard())
        // set duration to 10min
        onView(withId(R.id.addSlotDuration)).perform(ViewActions.scrollTo())
        // clear the former value and enter new value
        DigitSelector.pressClear(R.id.addSlotDuration)
        DigitSelector.pressDigit( DigitSelector.digitTwo, R.id.addSlotDuration)
        DigitSelector.pressDigit( DigitSelector.digitZero, R.id.addSlotDuration)
        // complete slot allocation
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())

        // perform action 5.2 (save transaction)
        onView(withId(R.id.ivSaveTransaction)).perform(click())

        // assert only 2 slots are saved in managementDB
        assert(instituteRepo.getObservableTimeSlotList().value != null)
        var allClientSlots = instituteRepo.getObservableTimeSlotList().value!!
            .filterIsInstance<ClientTimeSlot>()
        assert(allClientSlots.size == 2)

        // retrieve the new slot codes
        for (clientSlot in allClientSlots) {
            if (clientSlot.auxiliaryIdentifier == FIRST_SLOT_IDENTIFIER) {
                firstSlotCode = clientSlot.slotCode
            }
            if (clientSlot.auxiliaryIdentifier == SECOND_SLOT_IDENTIFIER) {
                secondSlotCode = clientSlot.slotCode
            }
        }

        // logout of management to get to loginFragment
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.logout()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // assert that we are logged out
        assertFalse(instituteRepo.isLoggedIn().value!!)

        // perform action 6 (input second slot code as client) and verify success
        onView(withId(R.id.etSlotCode))
            .perform(TextSetter.setTextEditText(secondSlotCode), ViewActions.closeSoftKeyboard())
        // assert that we are in remaining time fragment
        Thread.sleep(WAIT_FOR_UI_RESPONSE)
        onView(withId(R.id.btnRefresh)).check(matches(isDisplayed()))

        // login as management again (directly from repository, so we remain in remaining time fragment)
        instituteRepo.login(VALID_TEST_USERNAME, VALID_TEST_PASSWORD)
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        // check that we are logged in
        assertTrue(instituteRepo.isLoggedIn().value!!)

        // perform action 7 (directly from repository, so we remain in remaining time fragment)
        instituteRepo.newSpontaneousSlot(THIRD_SLOT_IDENTIFIER, Duration(
            THIRTY_FIVE_MINUTE_DURATION_MILLIS))
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        // save the transaction and the changes made
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.saveTransaction()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        // assert only 3 slots are saved in managementDB
        assert(instituteRepo.getObservableTimeSlotList().value != null)
        allClientSlots = instituteRepo.getObservableTimeSlotList().value!!
            .filterIsInstance<ClientTimeSlot>()
        assert(allClientSlots.size == 3)

        // retrieve the generated slotCode of the third slot
        for (clientSlot in allClientSlots) {
            if (clientSlot.auxiliaryIdentifier == THIRD_SLOT_IDENTIFIER) {
                thirdSlotCode = clientSlot.slotCode
            }
        }

        // perform action 8 (directly from repository, so we remain in remaining time fragment)
        // (move third slot between first and second slot)
        instituteRepo.moveSlotAfterAnother(thirdSlotCode, firstSlotCode)
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // perform action 9 (directly from repository, so we remain in remaining time fragment)
        // save the changes made
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.saveTransaction()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // check the result and assert expected result is met
        // notify for delay notification was called (exactly once) on the mock
        verify (exactly = 1)
        { injectedManager.notify(DELAY_NOTIFICATION_ID, any()) }
    }

}
