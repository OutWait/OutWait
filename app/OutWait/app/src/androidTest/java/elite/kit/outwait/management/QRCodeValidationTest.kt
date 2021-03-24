package elite.kit.outwait.management

import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
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
import util.DigitSelector
import javax.inject.Inject

@HiltAndroidTest
class QRCodeValidationTest {
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
    }

    //Half automatic T17 and T18
    @Test
    fun qrCodeValidation() {
        //Add first slot
        Espresso.onView(ViewMatchers.withId(R.id.floatingActionButton)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.etIdentifierAddDialog))
            .perform(ViewActions.typeText(FIRST_SLOT_IDENTIFIER), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.clear)).perform(ViewActions.click())
        DigitSelector.pressDigit(DigitSelector.digitFive, R.id.addSlotDuration)
        Espresso.onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(ViewActions.click())
        //Add second slot
        Espresso.onView(ViewMatchers.withId(R.id.floatingActionButton)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.etIdentifierAddDialog))
            .perform(ViewActions.typeText(SECOND_SLOT_IDENTIFIER), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.clear)).perform(ViewActions.click())
        DigitSelector.pressDigit(DigitSelector.digitTwo, R.id.addSlotDuration)
        DigitSelector.pressDigit(DigitSelector.digitZero, R.id.addSlotDuration)
        Espresso.onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(ViewActions.click())
        //Display slot information
        Espresso.onView(ViewMatchers.withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                SECOND_SLOT_TRANSACTION,
                ViewActions.click()
            )
        )
        //Save add slots
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.saveTransaction()
        }
        //Interaction: scan qr code from slot and check its duration: 4 min
        Thread.sleep(INTERACTION_TIME_LARGE)
        Espresso.onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(ViewActions.click())
        Espresso.onView(withText(StringResource.getResourceString(R.string.slot_details))).check(
            doesNotExist()
        )
    }

    @After
    fun emptySlot() {
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

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()
    }
}
