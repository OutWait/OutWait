package elite.kit.outwait.management

import android.app.Instrumentation
import android.util.Log
import android.view.KeyEvent
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
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
class StayInRemainingTimeFragmentTest {

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

    //T21
    @Test
    fun qrCodeValidation() {
        //Add first slot
        Espresso.onView(ViewMatchers.withId(R.id.floatingActionButton)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.etIdentifierAddDialog))
            .perform(ViewActions.typeText(FIRST_SLOT_IDENTIFIER), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.clear)).perform(ViewActions.click())
        DigitSelector.digitFive.perform(ViewActions.click())
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(ViewActions.click())
        //Save add slots
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.saveTransaction()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_SHORT)
        //Save first slot its slotcode
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT_POSITION,
                click()
            )
        )
        val firstPosSlotCode =
            ReadText.getText(onView(withId(R.id.tvSlotCodeDetail)))
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        onView(withId(R.id.config)).perform(click())
        onView(withId(R.id.btnLogout)).perform(click())
        onView(withId(R.id.etSlotCode)).perform(TextSetter.setTextEditText(firstPosSlotCode))
        Thread.sleep(WAIT_FOR_UI_RESPONSE)
        openActivityRule.scenario.close()
        Thread.sleep(INTERACTION_TIME_LARGE)
        onView(withId(R.id.tvRemainingTimeText)).check(matches(withText(StringResource.getResourceString(R.string.it_s_your_turn_in))))
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
