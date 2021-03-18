package elite.kit.outwait.management

import android.content.Context
import androidx.test.InstrumentationRegistry.getTargetContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import elite.kit.outwait.MainActivity
import elite.kit.outwait.R
import elite.kit.outwait.dataItem.TimeSlotItem
import elite.kit.outwait.recyclerviewSetUp.viewHolder.BaseViewHolder
import elite.kit.outwait.utils.EspressoIdlingResource
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val INSTITUTION_NAME_CORRECT = "test2"
private const val INSTITUTION_PASSWORD_CORRECT = "test2"
private const val SLOT_IDENTIFIER = "Hans"
private const val EMPTY_TEXT = ""
private const val FIRST_POSITION = 0



@RunWith(AndroidJUnit4::class)
class SlotDetailTest {
    @get:Rule
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Before
    fun loginAndAddSlot() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        //Input of correct login data
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
        //Verify of forwarding
        onView(withId(R.id.floatingActionButton)).perform(click())

        onView(withId(R.id.etIdentifierAddDialog)).perform(
            typeText(SLOT_IDENTIFIER),
            closeSoftKeyboard()
        )
        onView(withText(getResourceString(R.string.confirm))).perform(click())
        onView(withId(R.id.ivSaveTransaction)).perform(click())


    }

    //T26 and T27
    @Test
    fun showSlotDetail() {
        onView(withId(R.id.slotList)).perform(
            actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_POSITION,
                click()
            )
        )
        onView(withId(R.id.tvSlotCodeDetail)).check(matches(not(withText(EMPTY_TEXT))))
        onView(withId(R.id.tvIdentifierDetail)).check(matches(not(withText(EMPTY_TEXT))))
        onView(withId(R.id.tvDurationDetail)).check(matches(not(withText(EMPTY_TEXT))))
        onView(withText(getResourceString(R.string.confirm))).perform(click())
    }

    @After
    fun emptyQueue() {
        onView(withId(R.id.slotList)).perform(
            actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                0,
                swipeLeft()
            )
        )
        //Necessary due to no coroutine
        Thread.sleep(100)
        onView(withId(R.id.ivSaveTransaction)).perform(click())

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)


        openActivityRule.scenario.close()

    }

    private fun getResourceString(id: Int): String? {
        val targetContext: Context =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        return targetContext.resources.getString(id)
    }
}
