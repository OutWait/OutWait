package elite.kit.outwait.client

import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import elite.kit.outwait.MainActivity
import elite.kit.outwait.R
import elite.kit.outwait.customDataTypes.Mode
import elite.kit.outwait.customDataTypes.Preferences
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

@HiltAndroidTest
class NotificationAfterDelay {

    private lateinit var firstSlotCode: String
    private lateinit var secondSlotCode: String
    private lateinit var thirdSlotCode: String

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

    // tests T15
    @Test
    fun receiveDelayNotification() {

        /*
        // perform action 1 (open settings)
        Espresso.onView(ViewMatchers.withId(R.id.config)).perform(ViewActions.click())
        Thread.sleep(WAIT_FOR_UI_RESPONSE)
         */

        // perform action 1-3 //TODO mit DigitSelector als GUI Eingaben machen? Oder ohne GUI als s
        // second device simulieren?!
        val newPrefs = Preferences(Duration(DEFAULT_DURATION_MILLIS), Duration(DEFAULT_DURATION_MILLIS),
        Duration(THIRTY_MINUTE_DURATION_MILLIS), Duration(DEFAULT_DURATION_MILLIS), Mode.ONE)
        instituteRepo.changePreferences(newPrefs)
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // perform action 4 //TODO mit digit selector als gui eingabe machen
        instituteRepo.newSpontaneousSlot(FIRST_SLOT_IDENTIFIER, Duration(TEN_MINUTE_DURATION_MILLIS))
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // perform action 5 //TODO mit digit selector als gui eingabe machen
        instituteRepo.newSpontaneousSlot(SECOND_SLOT_IDENTIFIER, Duration(TWENTY_MINUTE_DURATION_MILLIS))
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // perform action 5.1 //TODO missing in Testdefinition (save transaction)
        // save the transaction and the changes made
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.saveTransaction()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // assert only 2 slots are saved in managementDB
        assert(instituteRepo.getObservableTimeSlotList().value != null)
        var allClientSlots = instituteRepo.getObservableTimeSlotList().value!!
            .filterIsInstance<ClientTimeSlot>()
        assert(allClientSlots.size == 2)

        // retrieve the slotCode of the added slots
        for (clientSlot in allClientSlots) {
            if (clientSlot.auxiliaryIdentifier == FIRST_SLOT_IDENTIFIER) {
                firstSlotCode = clientSlot.slotCode
            }
            if (clientSlot.auxiliaryIdentifier == SECOND_SLOT_IDENTIFIER) {
                secondSlotCode = clientSlot.slotCode
            }
        }

        // perform action 5.1 (logout of management to get to loginFragment) //TODO missing in TestDef.
        // logout of management
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.logout()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // check that we are logged out
        assert(!instituteRepo.isLoggedIn().value!!)

        // perform action 6 (enter second slotCode as client)
        Espresso.onView(ViewMatchers.withId(R.id.etSlotCode))
            .perform(TextSetter.setTextEditText(secondSlotCode), ViewActions.closeSoftKeyboard())
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // check if we navigated to remainingTimeFragment
        Espresso.onView(ViewMatchers.withId(R.id.btnRefresh)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )

        // login as management to change queue
        // perform login
        instituteRepo.login(
            VALID_TEST_USERNAME,
            VALID_TEST_PASSWORD
        )
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // check that we are logged in
        assert(instituteRepo.isLoggedIn().value!!)

        // perform action 7
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

        // perform action 8 (move third slot between first and second slot)
        instituteRepo.moveSlotAfterAnother(thirdSlotCode, firstSlotCode)
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // save the transaction and the changes made (action 8.2 //TODO missing in TestDef)
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.saveTransaction()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // logout of management
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.logout()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // check that we are logged out
        assert(!instituteRepo.isLoggedIn().value!!)

        // check for result
        // TODO Checken dass delay notification erzeugt wurde

    }

}
