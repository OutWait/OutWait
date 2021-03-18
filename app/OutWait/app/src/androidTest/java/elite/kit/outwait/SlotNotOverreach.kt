package elite.kit.outwait


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
class SlotNotOverreach {

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
        instituteRepo.login("test2", "test2")
    }

    //TODO testing except
    //TEST 25
    @Test
    fun slotRunsOn(){
        //Add first slot
        onView(withId(R.id.floatingActionButton)).perform(click())
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(FIRST_SLOT_IDENTIFIER), closeSoftKeyboard())
        onView(withId(R.id.clear)).perform(click())
        //Duration 2 min
        DigitSelector.digitTwo.perform(click())
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        //Add second slot
        onView(withId(R.id.floatingActionButton)).perform(click())
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(SECOND_SLOT_IDENTIFIER), closeSoftKeyboard())
        onView(withId(R.id.clear)).perform(click())
        //Duration 5 min
        DigitSelector.digitFive.perform(click())
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        //Add third slot
        onView(withId(R.id.floatingActionButton)).perform(click())
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(THIRD_SLOT_IDENTIFIER), closeSoftKeyboard())
        onView(withId(R.id.clear)).perform(click())
        DigitSelector.digitTwo.perform(click())
        DigitSelector.digitZero.perform(click())
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        //Save first slot its slotcode
        onView(withId(R.id.slotList)).perform(
            actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                THIRD_SLOT_TRANSACTION,
                click()
            )
        )
        val thirdPosSlotCode =
            ReadText.getText(onView(withId(R.id.tvSlotCodeDetail)))
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        onView(withId(R.id.ivSaveTransaction)).perform(click())
        onView(withId(R.id.config)).perform(click())
        onView(withId(R.id.btnLogout)).perform(click())
        onView(withId(R.id.etSlotCode)).perform(TextSetter.setTextEditText(thirdPosSlotCode))
        Thread.sleep(TIME_DECREASES)
        onView(withId(R.id.btn)).check(matches(withText(DECREASED_TIME)))
        Thread.sleep(TIME_STAGNATES)
        onView(withId(R.id.btn)).check(matches(withText(STAGNATED_TIME)))
    }

    @After
    fun emptySlot() {
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
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()
    }
}
