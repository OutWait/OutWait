package elite.kit.outwait.management

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText

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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
class CountOfClientsTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Inject
    lateinit var instituteRepo: InstituteRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Before
    fun addSlots() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)

        // login via injected repository
        instituteRepo.login(
            VALID_TEST_USERNAME,
            VALID_TEST_PASSWORD
        )

        //Verify of forwarding
        onView(withId(R.id.floatingActionButton)).perform(click())

        //Add first slot
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(FIRST_SLOT_IDENTIFIER), closeSoftKeyboard())
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())

        //Add second slot
        onView(withId(R.id.floatingActionButton)).perform(click())

        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(SECOND_SLOT_IDENTIFIER),closeSoftKeyboard())
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())

        //Add third slot
        onView(withId(R.id.floatingActionButton)).perform(click())

        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(THIRD_SLOT_IDENTIFIER), closeSoftKeyboard())
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        onView(withId(R.id.ivSaveTransaction)).perform(click())

    }

    //T30
    @Test
    fun areThreeClientsAdded() {
        onView(withId(R.id.config)).perform(click())
        onView(withId(R.id.countOfClients)).check(matches(withText(StringResource.getResourceString(R.string.text_counter) + THREE_CLIENTS)))
    }

    @After
    fun emptyQueue() {
        onView(ViewMatchers.isRoot()).perform(pressBack())

        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT_POSITION,
                swipeLeft()
            )
        )

        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT_TRANSACTION,
                swipeLeft()
            )
        )

        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT_TRANSACTION,
                swipeLeft()
            )
        )
        Thread.sleep(TRANSACTION_PAUSE)
        onView(withId(R.id.ivSaveTransaction)).perform(click())
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()

    }



}
