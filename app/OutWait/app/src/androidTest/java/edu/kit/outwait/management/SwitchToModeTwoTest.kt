package edu.kit.outwait.management

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import edu.kit.outwait.MainActivity
import edu.kit.outwait.R
import edu.kit.outwait.customDataTypes.Mode
import edu.kit.outwait.dataItem.TimeSlotItem
import edu.kit.outwait.instituteRepository.InstituteRepository
import edu.kit.outwait.recyclerviewSetUp.viewHolder.BaseViewHolder
import edu.kit.outwait.util.*
import edu.kit.outwait.utils.EspressoIdlingResource
import edu.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class SwitchToModeTwoTest {
    val auxIdentifier1 = "Slot1"
    val auxIdentifier2 = "Slot2"
    val auxIdentifier3 = "Slot3"
    val auxIdentifier4 = "Slot4"

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
        // Ensure mode one
        if (instituteRepo.getObservablePreferences().value!!.mode != Mode.ONE) {
            onView(withId(R.id.config)).perform(click())
            onView(withId(R.id.sMode)).perform(click())
            onView(withId(R.id.btnSave)).perform(scrollTo(), click())
            onView(isRoot()).perform((pressBack()))
            Thread.sleep(WAIT_FOR_UI_RESPONSE)
        }
    }

    fun clearQueue() {
        Thread.sleep(WAIT_FOR_UI_RESPONSE)
        val timeSlots = instituteRepo.getObservableTimeSlotList().value

        if (timeSlots != null && timeSlots.isNotEmpty()) {
            for (slot in timeSlots.filterIsInstance<ClientTimeSlot>()) {
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

    /** Waits for the specified amount of seconds, but keeps the test valid */
    private fun safeWait(seconds: Int) {
        for (i in 0 until seconds) {
            Thread.sleep(1000)
            // NOP check
            onView(withText("INVALID_DEBUG_TEST_TEXT")).check(ViewAssertions.doesNotExist())
        }
    }

    // T28
    @Test
    fun switchToModeTwo() {
        // Check Management view
        onView(withId(R.id.floatingActionButton))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        // Check mode
        assertEquals(Mode.ONE, instituteRepo.getObservablePreferences().value!!.mode)
        val slot1Time = DateTime().plusMinutes(2)
        val slot2Time = slot1Time.plusMinutes(1)
        val slot3Time = slot2Time.plusMinutes(1)
        // Set mode and prioritization time
        onView(withId(R.id.config)).perform(click())
        if (instituteRepo.getObservablePreferences().value!!.mode != Mode.TWO) {
            onView(withId(R.id.sMode)).perform(click())
        }
        onView(withId(R.id.configPrioDuration)).perform(scrollTo())
        DigitSelector.pressClear(R.id.configPrioDuration)
        DigitSelector.pressDigit(DigitSelector.digitOne, R.id.configPrioDuration)
        onView(withId(R.id.btnSave)).perform(scrollTo(), click())
        onView(isRoot()).perform((pressBack()))
        Thread.sleep(WAIT_FOR_UI_RESPONSE)
        // Add slot1 (fix)
        onView(withId(R.id.floatingActionButton)).perform(ViewActions.click())
        onView(withId(R.id.cbIsFixedSlot)).perform(click())
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(auxIdentifier1), closeSoftKeyboard())
        onView(withId(R.id.addSlotDuration)).perform(scrollTo())
        DigitSelector.pressClear(R.id.addSlotDuration)
        DigitSelector.pressDigit(DigitSelector.digitOne, R.id.addSlotDuration)
        onView(withId(R.id.tpAppointmentTime)).perform(
            scrollTo(),
            AppointmentSetter.setAppointment(
                slot1Time.hourOfDay().get(),
                slot1Time.minuteOfHour().get()
            )
        )
        onView(withText(StringResource.getResourceString(R.string.confirm))).perform(click())
        // Add slot2 (fix)
        onView(withId(R.id.floatingActionButton)).perform(ViewActions.click())
        onView(withId(R.id.cbIsFixedSlot)).perform(click())
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(auxIdentifier2), closeSoftKeyboard())
        onView(withId(R.id.addSlotDuration)).perform(scrollTo())
        DigitSelector.pressClear(R.id.addSlotDuration)
        DigitSelector.pressDigit(DigitSelector.digitOne, R.id.addSlotDuration)
        onView(withId(R.id.tpAppointmentTime)).perform(
            scrollTo(),
            AppointmentSetter.setAppointment(
                slot2Time.hourOfDay().get(),
                slot2Time.minuteOfHour().get()
            )
        )
        onView(withText(StringResource.getResourceString(R.string.confirm))).perform(click())
        // Add slot3 (fix)
        onView(withId(R.id.floatingActionButton)).perform(ViewActions.click())
        onView(withId(R.id.cbIsFixedSlot)).perform(click())
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(auxIdentifier3), closeSoftKeyboard())
        onView(withId(R.id.addSlotDuration)).perform(scrollTo())
        DigitSelector.pressClear(R.id.addSlotDuration)
        DigitSelector.pressDigit(DigitSelector.digitOne, R.id.addSlotDuration)
        onView(withId(R.id.tpAppointmentTime)).perform(
            scrollTo(),
            AppointmentSetter.setAppointment(
                slot3Time.hourOfDay().get(),
                slot3Time.minuteOfHour().get()
            )
        )
        onView(withText(StringResource.getResourceString(R.string.confirm))).perform(click())
        // Save transaction and wait 120 seconds (so that the first slot has started)
        onView(withId(R.id.ivSaveTransaction)).perform(click())
        safeWait(120)
        // Add slot4 (spontaneous)
        onView(withId(R.id.floatingActionButton)).perform(ViewActions.click())
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(auxIdentifier4), closeSoftKeyboard())
        onView(withId(R.id.addSlotDuration)).perform(scrollTo())
        DigitSelector.pressClear(R.id.addSlotDuration)
        DigitSelector.pressDigit(DigitSelector.digitOne, R.id.addSlotDuration)
        onView(withText(StringResource.getResourceString(R.string.confirm))).perform(click())
        // Save transaction
        onView(withId(R.id.ivSaveTransaction)).perform(click())
        // End slot1
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT_POSITION,
                swipeLeft()
            )
        )
        onView(withId(R.id.ivSaveTransaction)).perform(click())
        // End slot2
        safeWait(60)
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT_POSITION,
                swipeLeft()
            )
        )
        onView(withId(R.id.ivSaveTransaction)).perform(click())
        Thread.sleep(WAIT_FOR_UI_RESPONSE)
        // Check queue
        assertEquals(2, instituteRepo.getObservableTimeSlotList().value!!.size)
        assertEquals(
            auxIdentifier4,
            (instituteRepo.getObservableTimeSlotList()
                .value!![0] as ClientTimeSlot).auxiliaryIdentifier
        )
        assertEquals(
            auxIdentifier3,
            (instituteRepo.getObservableTimeSlotList()
                .value!![1] as ClientTimeSlot).auxiliaryIdentifier
        )
    }

    fun logoutRoutine() {
        // Logout
        onView(withId(R.id.config)).perform(click())
        onView(withId(R.id.btnLogout)).perform(click())
    }

    @After
    fun shutdownTest() {
        clearQueue()
        // Enable mode one again
        onView(withId(R.id.config)).perform(click())
        onView(withId(R.id.sMode)).perform(click())
        onView(withId(R.id.btnSave)).perform(scrollTo(), click())
        onView(isRoot()).perform((pressBack()))
        Thread.sleep(WAIT_FOR_UI_RESPONSE)

        logoutRoutine()

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()
    }
}
