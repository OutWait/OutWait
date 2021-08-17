package edu.kit.outwait.management

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
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
import edu.kit.outwait.util.StringResource.getResourceString
import edu.kit.outwait.utils.EspressoIdlingResource
import edu.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@HiltAndroidTest
class SlotDetailTest {
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
        //Login as management
        instituteRepo.login(VALID_TEST_USERNAME, VALID_TEST_PASSWORD)

        onView(withId(R.id.floatingActionButton)).perform(click())
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(ViewActions.typeText(SLOT_IDENTIFIER), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.clear)).perform(click())
        DigitSelector.pressDigit(DigitSelector.digitFive, R.id.addSlotDuration)
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        onView(withId(R.id.ivSaveTransaction)).perform(click())
    }

    //T26 and T27
    @Test
    fun showSlotDetail() {
        onView(withId(R.id.slotList)).perform(
            actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT_POSITION,
                click()
            )
        )
        onView(withId(R.id.tvSlotCodeDetail)).check(matches(not(withText(EMPTY_TEXT))))
        onView(withId(R.id.tvIdentifierDetail)).check(matches((withText(SLOT_IDENTIFIER))))
        onView(withId(R.id.tvDurationDetail)).check(matches((withText(FIVE_MINUTES_DURATION))))
        onView(withText(getResourceString(R.string.confirm))).perform(click())
    }

    @After
    fun emptyQueue() {
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
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.logout()
        }
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()

    }


}
