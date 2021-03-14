package elite.kit.outwait

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText

import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elite.kit.outwait.*
import elite.kit.outwait.dataItem.TimeSlotItem
import elite.kit.outwait.recyclerviewSetUp.viewHolder.BaseViewHolder
import elite.kit.outwait.util.StringResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val INSTITUTION_NAME_CORRECT = "test2"
private const val INSTITUTION_PASSWORD_CORRECT = "test2"
private const val SLOT_IDENTIFIER_ONE = "First person"
private const val SLOT_IDENTIFIER_TWO = "Second person"
private const val SLOT_IDENTIFIER_THREE = "Third person"
private const val FIRST_SLOT     = 0
private const val FIRST_SLOT_TRANSACTION     = 1



private const val THREE = "3"

@RunWith(AndroidJUnit4::class)
class CountOfClientsTest {
    @get:Rule
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Before
    fun addSlots() {
        //Login
        onView(withId(R.id.etInstituteName))
            .perform(
                typeText(INSTITUTION_NAME_CORRECT),
                closeSoftKeyboard()
            )
        onView(withId(R.id.etInstitutePassword))
            .perform(
                typeText(INSTITUTION_PASSWORD_CORRECT),
                closeSoftKeyboard()
            )
        onView(withId(R.id.btnLoginFrag)).perform(click())
        Thread.sleep(4000)
        //Verify of forwarding
        onView(withId(R.id.floatingActionButton)).perform(click())
        Thread.sleep(2000)
        //Add first slot
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(SLOT_IDENTIFIER_ONE), closeSoftKeyboard())
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        Thread.sleep(2000)
        //Add second slot
        onView(withId(R.id.floatingActionButton)).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(SLOT_IDENTIFIER_TWO),closeSoftKeyboard())
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        Thread.sleep(2000)
        //Add third slot
        onView(withId(R.id.floatingActionButton)).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(SLOT_IDENTIFIER_THREE), closeSoftKeyboard())
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())
        Thread.sleep(2000)

        onView(withId(R.id.ivSaveTransaction)).perform(click())

        Thread.sleep(4000)
    }

    //T30
    @Test
    fun areThreeClientsAdded() {
        onView(withId(R.id.config)).perform(click())
        Thread.sleep(1000)
        onView(withId(R.id.countOfClients)).check(matches(withText(StringResource.getResourceString(R.string.text_counter) + THREE)))
    }

    @After
    fun emptyQueue() {
        onView(ViewMatchers.isRoot()).perform(pressBack())
        Thread.sleep(1000)
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT,
                swipeLeft()
            )
        )
        Thread.sleep(4000)
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT_TRANSACTION,
                swipeLeft()
            )
        )
        Thread.sleep(4000)
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT_TRANSACTION,
                swipeLeft()
            )
        )
        Thread.sleep(3100)
        onView(withId(R.id.ivSaveTransaction)).perform(click())
        openActivityRule.scenario.close()
    }

}
