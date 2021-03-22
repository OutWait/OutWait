package  util

import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers
import elite.kit.outwait.R
import elite.kit.outwait.util.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher

object DigitSelector {
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

    val digitTwo: ViewInteraction = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.numPad2),
            ViewMatchers.withText( SLOT_DURATION_TWO),
            childAtPosition(
                childAtPosition(
                    ViewMatchers.withId(R.id.numPad),
                    FIRST_ROW
                ),
                SECOND_COLUMN
            ),
            ViewMatchers.isDisplayed()
        )
    )

    val digitZero :ViewInteraction  = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.numPad0),
            ViewMatchers.withText(SLOT_DURATION_ZERO),
            childAtPosition(
                childAtPosition(
                    ViewMatchers.withId(R.id.numPad),
                    FOURTH_ROW
                ),
                SECOND_COLUMN
            ),
            ViewMatchers.isDisplayed()
        )
    )

    val digitFive :ViewInteraction  = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.numPad5),
            ViewMatchers.withText( SLOT_DURATION_FIVE),
            childAtPosition(
                childAtPosition(
                    ViewMatchers.withId(R.id.numPad),
                    SECOND_ROW
                ),
                SECOND_COLUMN
            ),
            ViewMatchers.isDisplayed()
        )
    )

    val digitEight: ViewInteraction  = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.numPad8),
            ViewMatchers.withText( SLOT_DURATION_EIGHT),
            childAtPosition(
                childAtPosition(
                    ViewMatchers.withId(R.id.numPad),
                    THIRD_ROW
                ),
                SECOND_COLUMN
            ),
            ViewMatchers.isDisplayed()
        )
    )
}
