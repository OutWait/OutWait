package elite.kit.outwait

import android.content.Context
import androidx.test.InstrumentationRegistry.getTargetContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
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
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val INSTITUTION_NAME_CORRECT="test2"
private const val INSTITUTION_PASSWORD_CORRECT="test2"
private const val SLOT_IDENTIFIER="Hans"
@RunWith(AndroidJUnit4::class)
class SlotDetailTest {

    @get:Rule
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Before
    fun loginAndAddSlot(){
        //Input of correct login data
        Espresso.onView(ViewMatchers.withId(R.id.etInstituteName))
            .perform(
                ViewActions.typeText(INSTITUTION_NAME_CORRECT),
                ViewActions.closeSoftKeyboard()
            )
        Espresso.onView(ViewMatchers.withId(R.id.etInstitutePassword))
            .perform(
                ViewActions.typeText(INSTITUTION_PASSWORD_CORRECT),
                ViewActions.closeSoftKeyboard()
            )
        Espresso.onView(ViewMatchers.withId(R.id.btnLoginFrag)).perform(ViewActions.click())
        Thread.sleep(8000)
        //Verify of forwarding
        onView(withId(R.id.floatingActionButton)).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.etIdentifierAddDialog)).perform(typeText(SLOT_IDENTIFIER))
        onView(withText(getResourceString(R.string.confirm))).perform(click())
        onView(withId(R.id.ivSaveTransaction)).perform(click())

        Thread.sleep(4000)


    }

    //T26 and T27
    @Test
    fun showSlotDetail(){
        onView(withId(R.id.slotList)).perform(
            actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                0,
                click()
            )
        )
        Thread.sleep(2000)
        onView(withId(R.id.tvSlotCodeDetail)).check(matches(not(withText(""))))
        onView(withId(R.id.tvIdentifierDetail)).check(matches(not(withText(""))))
        onView(withId(R.id.tvDurationDetail)).check(matches(not(withText(""))))
        onView(withText(getResourceString(R.string.confirm))).perform(click())
        Thread.sleep(3000)

    }

    @After
    fun emptyQueue(){
       onView(withId(R.id.slotList)).perform(
            actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                0,
                swipeLeft()
            )
        )
        Thread.sleep(5000)
        onView(withId(R.id.ivSaveTransaction)).perform(click())
        openActivityRule.scenario.close()

    }

    private fun getResourceString(id: Int): String? {
        val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        return targetContext.resources.getString(id)
    }
}
