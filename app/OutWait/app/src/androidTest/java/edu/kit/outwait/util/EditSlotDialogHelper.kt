package edu.kit.outwait.util

import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.contrib.RecyclerViewActions
import edu.kit.outwait.dataItem.TimeSlotItem
import edu.kit.outwait.recyclerviewSetUp.viewHolder.BaseViewHolder
import edu.kit.outwait.R
import edu.kit.outwait.util.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.Matchers.allOf

object EditSlotDialogHelper {
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

    class ClickOnEditIcon : ViewAction {
        val click = click()

        override fun getConstraints(): Matcher<View> {
            return click.getConstraints()
        }

        override fun getDescription(): String {
            return " click on custom image view"
        }

        override fun perform(uiController: UiController, view: View) {
            val v: View = view.findViewById(R.id.ivEditIconSpo) ?: view.findViewById(R.id.ivEditIconFix)!!
            click.perform(uiController, v)
        }
    }

    fun openEditDialog(slotIndex: Int) {
        Espresso.onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                slotIndex,
                ClickOnEditIcon()
            )
        )
    }
}
