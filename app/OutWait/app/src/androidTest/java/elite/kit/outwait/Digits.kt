package elite.kit.outwait


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class Digits {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun digits() {
        val clipToBoundsView = onView(
            allOf(
                withId(R.id.text_field_boxes_panel),
                childAtPosition(
                    allOf(
                        withId(R.id.text_field_boxes_right_shell),
                        childAtPosition(
                            withClassName(`is`("android.widget.RelativeLayout")),
                            1
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        clipToBoundsView.perform(click())

        val extendedEditText = onView(
            allOf(
                withId(R.id.etInstituteName),
                childAtPosition(
                    allOf(
                        withId(R.id.text_field_boxes_input_layout),
                        childAtPosition(
                            withId(R.id.text_field_boxes_editTextLayout),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        extendedEditText.perform(replaceText("test2"), closeSoftKeyboard())

        pressBack()

        val extendedEditText2 = onView(
            allOf(
                withId(R.id.etInstituteName), withText("test2"),
                childAtPosition(
                    allOf(
                        withId(R.id.text_field_boxes_input_layout),
                        childAtPosition(
                            withId(R.id.text_field_boxes_editTextLayout),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        extendedEditText2.perform(replaceText("test2 "))

        val extendedEditText3 = onView(
            allOf(
                withId(R.id.etInstituteName), withText("test2 "),
                childAtPosition(
                    allOf(
                        withId(R.id.text_field_boxes_input_layout),
                        childAtPosition(
                            withId(R.id.text_field_boxes_editTextLayout),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        extendedEditText3.perform(closeSoftKeyboard())

        val clipToBoundsView2 = onView(
            allOf(
                withId(R.id.text_field_boxes_panel),
                childAtPosition(
                    allOf(
                        withId(R.id.text_field_boxes_right_shell),
                        childAtPosition(
                            withClassName(`is`("android.widget.RelativeLayout")),
                            1
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        clipToBoundsView2.perform(click())

        val extendedEditText4 = onView(
            allOf(
                withId(R.id.etInstitutePassword),
                childAtPosition(
                    allOf(
                        withId(R.id.text_field_boxes_input_layout),
                        childAtPosition(
                            withId(R.id.text_field_boxes_editTextLayout),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        extendedEditText4.perform(replaceText("test2"), closeSoftKeyboard())

        pressBack()

        val button = onView(
            allOf(
                withId(R.id.btnLoginFrag), withText("Anmelden"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.main_container),
                        0
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        button.perform(click())

        val floatingActionButton = onView(
            allOf(
                withId(R.id.floatingActionButton),
                childAtPosition(
                    allOf(
                        withId(R.id.managementViewContainer),
                        childAtPosition(
                            withId(R.id.main_container),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        floatingActionButton.perform(click())

        val appCompatImageButton = onView(
            allOf(
                withId(R.id.clear),
                childAtPosition(
                    allOf(
                        withId(R.id.displayRow),
                        childAtPosition(
                            withId(R.id.addSlotDuration),
                            0
                        )
                    ),
                    2
                )
            )
        )
        appCompatImageButton.perform(scrollTo(), click())

        val materialButton = onView(
            allOf(
                withId(R.id.numPad2), withText("2"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.numPad),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        materialButton.perform(click())

        val materialButton2 = onView(
            allOf(
                withId(R.id.numPad1), withText("1"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.numPad),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        materialButton2.perform(click())

        val materialButton3 = onView(
            allOf(
                withId(R.id.numPad3), withText("3"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.numPad),
                        0
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        materialButton3.perform(click())

        val materialButton4 = onView(
            allOf(
                withId(R.id.numPad0), withText("0"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.numPad),
                        3
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        materialButton4.perform(click())

        val materialButton5 = onView(
            allOf(
                withId(android.R.id.button1), withText("OK"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.ScrollView")),
                        0
                    ),
                    3
                )
            )
        )
        materialButton5.perform(scrollTo(), click())
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
