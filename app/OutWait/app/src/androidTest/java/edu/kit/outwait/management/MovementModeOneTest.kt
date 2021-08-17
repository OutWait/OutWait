package edu.kit.outwait.management

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import edu.kit.outwait.MainActivity
import edu.kit.outwait.R
import edu.kit.outwait.dataItem.TimeSlotItem
import edu.kit.outwait.instituteRepository.InstituteRepository
import edu.kit.outwait.recyclerviewSetUp.viewHolder.BaseViewHolder
import edu.kit.outwait.util.*
import edu.kit.outwait.util.ReadText.getText
import edu.kit.outwait.utils.EspressoIdlingResource
import edu.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class MovementModeOneTest {
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
        instituteRepo.login(VALID_TEST_USERNAME, VALID_TEST_PASSWORD)
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        // check that we are logged in
        assert(instituteRepo.isLoggedIn().value!!)
        // clean up waiting queue (on server side also)
        val timeSlots = instituteRepo.getObservableTimeSlotList().value


        if (timeSlots != null && timeSlots.isNotEmpty()) {
            val onlyClientSlots: List<ClientTimeSlot> = timeSlots.filterIsInstance<ClientTimeSlot>()
            for (ClientTimeSlot in onlyClientSlots) {
                // delete slot with retrieved slotCode from waiting queue
                instituteRepo.deleteSlot(ClientTimeSlot.slotCode)
                Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
            }
            // save the transaction and the changes made
            CoroutineScope(Dispatchers.Main).launch {
                instituteRepo.saveTransaction()
            }
        }
    }

    //TEST 9
    @Test
    fun moveSlots() {
        //Add first slot
        onView(withId(R.id.floatingActionButton)).perform(click())
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(ViewActions.typeText(FIRST_SLOT_IDENTIFIER), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.clear)).perform(click())
        DigitSelector.pressDigit(DigitSelector.digitOne, R.id.addSlotDuration)
        DigitSelector.pressDigit(DigitSelector.digitZero, R.id.addSlotDuration)
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        //Save first slot its slotcode
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT_TRANSACTION,
                click()
            )
        )
        val firstPosSlotCode = getText(onView(withId(R.id.tvSlotCodeDetail)))
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        //Add second slot
        onView(withId(R.id.floatingActionButton)).perform(click())
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(ViewActions.typeText(SECOND_SLOT_IDENTIFIER), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.clear)).perform(click())
        DigitSelector.pressDigit(DigitSelector.digitTwo, R.id.addSlotDuration)
        DigitSelector.pressDigit(DigitSelector.digitZero, R.id.addSlotDuration)
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        //Save second slot its slotcode
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                SECOND_SLOT_TRANSACTION,
                click()
            )
        )
        val secondPosSlotCode = getText(onView(withId(R.id.tvSlotCodeDetail)))
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        //Add third slot
        onView(withId(R.id.floatingActionButton)).perform(click())
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(ViewActions.typeText(THIRD_SLOT_IDENTIFIER), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.clear)).perform(click())
        DigitSelector.pressDigit(DigitSelector.digitThree, R.id.addSlotDuration)
        DigitSelector.pressDigit(DigitSelector.digitZero, R.id.addSlotDuration)
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        //Save third slot its slotcode
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                THIRD_SLOT_TRANSACTION,
                click()
            )
        )
        val thirdPosSlotCode = getText(onView(withId(R.id.tvSlotCodeDetail)))
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        //Add fourth slot
        onView(withId(R.id.floatingActionButton)).perform(click())
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(ViewActions.typeText(FOURTH_SLOT_IDENTIFIER), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.clear)).perform(click())
        DigitSelector.pressDigit(DigitSelector.digitFour, R.id.addSlotDuration)
        DigitSelector.pressDigit(DigitSelector.digitZero, R.id.addSlotDuration)
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        //Save fourth slot its slotcode
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FOURTH_SLOT_TRANSACTION,
                click()
            )
        )
        val fourthPosSlotCode = getText(onView(withId(R.id.tvSlotCodeDetail)))
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        //Drag and drop fourth slot at position after first
        instituteRepo.moveSlotAfterAnother(fourthPosSlotCode, firstPosSlotCode)
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.saveTransaction()
        }
        //Check right order
        //First slot
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT_POSITION,
                click()
            )
        )
        onView(withId(R.id.tvSlotCodeDetail)).check(matches(withText(firstPosSlotCode)))
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        //Second slot former fourth slot
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                SECOND_SLOT_POSITION,
                click()
            )
        )
        onView(withId(R.id.tvSlotCodeDetail)).check(matches(withText(fourthPosSlotCode)))
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        //Third slot
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                THIRD_SLOT_POSITION,
                click()
            )
        )
        onView(withId(R.id.tvSlotCodeDetail)).check(matches(withText(secondPosSlotCode)))
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        //Fourth slot former third slot
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FOURTH_SLOT_POSITION,
                click()
            )
        )
        onView(withId(R.id.tvSlotCodeDetail)).check(matches(withText(thirdPosSlotCode)))
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())

    }

    @After
    fun emptySlot() {
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.logout()
        }
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()
    }
}
