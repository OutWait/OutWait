package elite.kit.outwait

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MainActivityTest{
    @get:Rule
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Test
    fun isActivityInView(){
        onView(withId(R.id.main)).check(matches(isDisplayed()))
    }
}
