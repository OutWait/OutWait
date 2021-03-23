package elite.kit.outwait.management


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import elite.kit.outwait.MainActivity
import elite.kit.outwait.R
import elite.kit.outwait.dataItem.TimeSlotItem
import elite.kit.outwait.instituteRepository.InstituteRepository
import elite.kit.outwait.recyclerviewSetUp.viewHolder.BaseViewHolder
import elite.kit.outwait.util.*
import elite.kit.outwait.utils.EspressoIdlingResource
import elite.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import util.DigitSelector
import javax.inject.Inject

private const val INSTITUTION_NAME_CORRECT = "test2"
private const val INSTITUTION_PASSWORD_CORRECT = "test2"
private const val SLOT_IDENTIFIER_ONE = "Slot1"
private const val SLOT_IDENTIFIER_TWO = "Slot2"
private const val SLOT_IDENTIFIER_THREE = "Slot3"
private const val FIRST_DURATION = "00:02"
private const val SECOND_DURATION = "00:25"
private const val THIRD_DURATION = "00:05"
private const val FIRST_SLOT_TRANSACTION = 1


@HiltAndroidTest
class ManagementAddSlotsTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Inject
    lateinit var instituteRepo: InstituteRepository

    @Before
    fun loginAndModeOne() {
        hiltRule.inject()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        //Login
        instituteRepo.login(VALID_TEST_USERNAME, VALID_TEST_PASSWORD)
    }

    //TEST 6
    @Test
    fun addSlots() {
        //Add first slot
        onView(withId(R.id.floatingActionButton)).perform(click())
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(SLOT_IDENTIFIER_ONE), closeSoftKeyboard())
        onView(withId(R.id.clear)).perform(click())
        //Type Duration
        DigitSelector.digitTwo.perform(click())
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        //Add second slot
        onView(withId(R.id.floatingActionButton)).perform(click())
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(SLOT_IDENTIFIER_TWO), closeSoftKeyboard())
        onView(withId(R.id.clear)).perform(click())
        //Type Duration
        DigitSelector.digitTwo.perform(click())
        DigitSelector.digitFive.perform(click())
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        //Add third slot
        onView(withId(R.id.floatingActionButton)).perform(click())
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(SLOT_IDENTIFIER_THREE), closeSoftKeyboard())
        onView(withId(R.id.clear)).perform(click())
        //Type Duration
        DigitSelector.digitFive.perform(click())
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        //Save
        onView(withId(R.id.ivSaveTransaction)).perform(click())
        //Verify order
        onView(withId(R.id.slotList)).perform(
            actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT_POSITION,
                click()
            )
        )
        onView(withId(R.id.tvDurationDetail))
            .check(matches((withText(FIRST_DURATION))))
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        onView(withId(R.id.slotList)).perform(
            actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                SECOND_SLOT_POSITION,
                click()
            )
        )
        onView(withId(R.id.tvDurationDetail))
            .check(matches((withText(SECOND_DURATION))))
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        onView(withId(R.id.slotList)).perform(
            actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                THIRD_SLOT_POSITION,
                click()
            )
        )
        onView(withId(R.id.tvDurationDetail))
            .check(matches((withText(THIRD_DURATION))))
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
    }

    @After
    fun emptyQueue() {
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
        }

        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.logout()
        }

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()
    }
}
