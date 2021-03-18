package elite.kit.outwait

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import elite.kit.outwait.util.ToastMatcher
import org.junit.Rule
import org.junit.Test


@HiltAndroidTest
class ToastMatchTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Test
    fun matchToast(){


        onView(withId(R.id.btnLoginFrag)).perform(click())
       // Thread.sleep(3000)
        onView(withText("Anmeldung fehlgeschlagen!")).inRoot(ToastMatcher()).check(matches(isDisplayed()))


        openActivityRule.scenario.close()
    }
}
