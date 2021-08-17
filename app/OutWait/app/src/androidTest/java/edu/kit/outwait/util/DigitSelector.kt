package edu.kit.outwait.util

import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import edu.kit.outwait.R
import edu.kit.outwait.util.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.Matchers.allOf

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

    fun forceClick(): ViewAction {
        return object: ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(isClickable(), isEnabled(), isDisplayed());
            }

            override fun getDescription(): String {
                return "force click";
            }

            override fun perform(uiController: UiController, view: View) {
                view.performClick(); // perform click without checking view coordinates.
                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    data class Digit(val rId: Int, val text: String, val row: Int, val column: Int)

    val digitOne = Digit(R.id.numPad1, "1", 0, 0)
    val digitTwo = Digit(R.id.numPad2, "2", 0, 1)
    val digitThree = Digit(R.id.numPad3, "3", 0, 2)
    val digitFour = Digit(R.id.numPad4, "4", 1, 0)
    val digitFive = Digit(R.id.numPad5, "5", 1, 1)
    val digitSix = Digit(R.id.numPad6, "6", 1, 2)
    val digitSeven = Digit(R.id.numPad7, "7", 2, 0)
    val digitEight = Digit(R.id.numPad8, "8", 2, 1)
    val digitNine = Digit(R.id.numPad9, "9", 2, 2)
    val digitZero = Digit(R.id.numPad0, "0", 3, 1)

    fun pressDigit(digit: Digit, padRId: Int) {
        Espresso.onView(Matchers.allOf(
            ViewMatchers.withId(digit.rId),
            ViewMatchers.withText(digit.text),
            childAtPosition(
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(padRId),
                        1
                    ),
                    digit.row
                ),
                digit.column
            ),
            ViewMatchers.isDisplayed()
        )).perform(forceClick())
    }

    fun pressClear(padRId: Int) {
        val appCompatImageButton = Espresso.onView(
            allOf(
                withId(R.id.clear),
                childAtPosition(
                    allOf(
                        withId(R.id.displayRow),
                        childAtPosition(
                            withId(padRId),
                            0
                        )
                    ),
                    2
                )
            )
        )
        appCompatImageButton.perform(scrollTo(), click())
    }
}
