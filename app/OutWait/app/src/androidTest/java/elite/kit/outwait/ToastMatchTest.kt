package elite.kit.outwait

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import elite.kit.outwait.util.ToastMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ToastMatchTest {
    @get:Rule
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Test
    fun matchToast(){


        onView(withId(R.id.btnLoginFrag)).perform(click())
       // Thread.sleep(3000)
        onView(withText("Login denied!")).inRoot(ToastMatcher())
            .check(matches(isDisplayed()))
        openActivityRule.scenario.close()
    }
}
