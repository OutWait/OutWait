package elite.kit.outwait.management

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.contrib.RecyclerViewActions
import elite.kit.outwait.dataItem.TimeSlotItem
import elite.kit.outwait.recyclerviewSetUp.viewHolder.BaseViewHolder
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import android.view.View
import elite.kit.outwait.MainActivity
import elite.kit.outwait.R
import elite.kit.outwait.instituteRepository.InstituteRepository
import elite.kit.outwait.util.StringResource
import elite.kit.outwait.util.ToastMatcher
import elite.kit.outwait.util.*
import elite.kit.outwait.util.DigitSelector
import elite.kit.outwait.utils.EspressoIdlingResource
import elite.kit.outwait.customDataTypes.Mode
import elite.kit.outwait.customDataTypes.Preferences
import elite.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import javax.inject.Inject
import org.joda.time.DateTime
import org.joda.time.Duration
import org.hamcrest.Matchers.allOf

@HiltAndroidTest
class ReLoginTest {
    val auxIdentifier = "Herr Meier"

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Inject
    lateinit var instituteRepo: InstituteRepository


    @Before
    fun initTest() {
        hiltRule.inject()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        instituteRepo.login(VALID_TEST_USERNAME, VALID_TEST_PASSWORD)

        clearQueue()
    }

    fun clearQueue() {
        Thread.sleep(WAIT_FOR_UI_RESPONSE)
        val timeSlots = instituteRepo.getObservableTimeSlotList().value

        if (timeSlots != null && timeSlots.isNotEmpty()) {
            for (slot in timeSlots.filterIsInstance<ClientTimeSlot>()){
                // Delete slot with retrieved slotCode from waiting queue.
                instituteRepo.deleteSlot(slot.slotCode)
                Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
            }
            // Save the transaction and the changes made.
            CoroutineScope(Dispatchers.Main).launch {
                instituteRepo.saveTransaction()
            }
        }
    }

    // T3
    @Test
    fun reLogin(){
        // Check Management view
        onView(withId(R.id.floatingActionButton))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // Add a new slot
        onView(withId(R.id.floatingActionButton)).perform(ViewActions.click())
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(auxIdentifier), closeSoftKeyboard())
            // Set initial duration
        onView(withId(R.id.addSlotDuration)).perform(scrollTo())
        onView(withId(R.id.clear)).perform(click())
        DigitSelector.pressDigit(DigitSelector.digitTwo, R.id.addSlotDuration)
        DigitSelector.pressDigit(DigitSelector.digitZero, R.id.addSlotDuration)
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())

        // Save transaction
        onView(withId(R.id.ivSaveTransaction)).perform(click())
        Thread.sleep(WAIT_FOR_UI_RESPONSE)

        // Get the slot code
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        val slotCode = (instituteRepo.getObservableTimeSlotList().getValue()!![0] as ClientTimeSlot).slotCode

        // Logout
        logoutRoutine()

        // Re-login
        instituteRepo.login(VALID_TEST_USERNAME, VALID_TEST_PASSWORD)

        // Check Management view
        onView(withId(R.id.floatingActionButton))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // Check the queue and code
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        assertEquals(1, instituteRepo.getObservableTimeSlotList().getValue()!!.size)
        assertEquals(slotCode, (instituteRepo.getObservableTimeSlotList().getValue()!![0] as ClientTimeSlot).slotCode)

        // Check aux identifier
        onView(withText(auxIdentifier))
            .check(ViewAssertions.doesNotExist())

        // Check slot duration
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT_POSITION,
                click()
            )
        )
        onView(withId(R.id.tvDurationDetail))
            .check(ViewAssertions.matches(ViewMatchers.withText("00:20")))

        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
    }

    fun logoutRoutine() {
        // Logout
        onView(withId(R.id.config)).perform(click())
        onView(withId(R.id.btnLogout)).perform(click())
    }

    @After
    fun shutdownTest() {
        clearQueue()

        logoutRoutine()

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()
    }
}
