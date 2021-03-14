package elite.kit.outwait

import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import elite.kit.outwait.dataItem.TimeSlotItem
import elite.kit.outwait.recyclerviewSetUp.viewHolder.BaseViewHolder
import elite.kit.outwait.util.StringResource
import org.hamcrest.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val INSTITUTION_NAME_CORRECT = "test2"
private const val INSTITUTION_PASSWORD_CORRECT = "test2"
private const val SLOT_IDENTIFIER_ONE = "Slot1"
private const val SLOT_IDENTIFIER_TWO = "Slot2"
private const val SLOT_IDENTIFIER_THREE = "Slot3"
private const val SLOT_DURATION_TWO = "2"
private const val SLOT_DURATION_FIVE = "5"
private const val SLOT_DURATION_ZERO = "0"
private const val FIRST_DURATION = "00:20"
private const val SECOND_DURATION = "00:25"
private const val THIRD_DURATION = "00:50"
private const val FIRST_SLOT = 0
private const val SECOND_SLOT = 1
private const val THIRD_SLOT = 2

private const val FIRST_SLOT_TRANSACTION = 1

@RunWith(AndroidJUnit4::class)
class AddSlotsTest {

    @get:Rule
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Before
    fun loginAndModeOne() {
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
        onView(withId(R.id.btnLoginFrag)).perform(ViewActions.click())
        Thread.sleep(4000)
        //TODO save in the account always mode 1
        //onView(withId(R.id.sMode)).perform(click())

    }

    @Test
    fun addSlots() {
        //Add first slot
        onView(withId(R.id.floatingActionButton)).perform(ViewActions.click())
        Thread.sleep(2000)
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(SLOT_IDENTIFIER_ONE), closeSoftKeyboard())

        onView(withId(R.id.clear)).perform(click())


        val digitTwo = onView(
            Matchers.allOf(
                withId(R.id.numPad2), withText(SLOT_DURATION_TWO),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.numPad),
                        0
                    ),
                    1
                ),
                ViewMatchers.isDisplayed()
            )
        )
        digitTwo.perform(click())

        val digitZero = onView(
            Matchers.allOf(
                withId(R.id.numPad0), withText(SLOT_DURATION_ZERO),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.numPad),
                        3
                    ),
                    1
                ),
                ViewMatchers.isDisplayed()
            )
        )
        digitZero.perform(click())



        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(ViewActions.click())
        Thread.sleep(2000)

        //Add second slot
        onView(withId(R.id.floatingActionButton)).perform(ViewActions.click())
        Thread.sleep(2000)
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(SLOT_IDENTIFIER_TWO), closeSoftKeyboard())
        onView(withId(R.id.clear)).perform(click())



        digitTwo.perform(click())

        val digitFive = onView(
            Matchers.allOf(
                withId(R.id.numPad5), withText(SLOT_DURATION_FIVE),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.numPad),
                        1
                    ),
                    1
                ),
                ViewMatchers.isDisplayed()
            )
        )
        digitFive.perform(click())

        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(ViewActions.click())
        Thread.sleep(2000)

        //Add third slot
        onView(withId(R.id.floatingActionButton)).perform(ViewActions.click())
        Thread.sleep(2000)
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(SLOT_IDENTIFIER_THREE), closeSoftKeyboard())
        onView(withId(R.id.clear)).perform(click())


        digitFive.perform(click())


        digitZero.perform(click())


        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(ViewActions.click())
        Thread.sleep(2000)

        //Save

        onView(withId(R.id.ivSaveTransaction)).perform(ViewActions.click())
        Thread.sleep(4000)

        //Verify order
        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT,
                click()
            )
        )
        onView(withId(R.id.tvDurationDetail)).check(matches((withText(FIRST_DURATION))))
        onView(withText(StringResource.getResourceString(R.string.confirm))).perform(click())

        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                SECOND_SLOT,
                click()
            )
        )
        onView(withId(R.id.tvDurationDetail)).check(matches((withText(SECOND_DURATION))))
        onView(withText(StringResource.getResourceString(R.string.confirm))).perform(click())

        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                THIRD_SLOT,
                click()
            )
        )
        onView(withId(R.id.tvDurationDetail)).check(matches((withText(THIRD_DURATION))))
        onView(withText(StringResource.getResourceString(R.string.confirm))).perform(click())


    }

    @After
    fun emptyQueue() {
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

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                    && view == parent.getChildAt(position)
            }
        }
    }
}
