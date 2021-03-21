package elite.kit.outwait.management

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import elite.kit.outwait.MainActivity
import elite.kit.outwait.R
import elite.kit.outwait.util.VALID_TEST_PASSWORD
import elite.kit.outwait.util.VALID_TEST_USERNAME
import elite.kit.outwait.utils.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith



@HiltAndroidTest
class ManagementLoginTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var openActivityRule = activityScenarioRule<MainActivity>()


    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    //T1
    @Test
    fun loginSuccessfully() {

        //Input of correct login data
        onView(withId(R.id.etInstituteName))
            .perform(
                ViewActions.typeText(VALID_TEST_USERNAME),
                ViewActions.closeSoftKeyboard()
            )
        onView(withId(R.id.etInstitutePassword))
            .perform(
                ViewActions.typeText(VALID_TEST_PASSWORD),
                ViewActions.closeSoftKeyboard()
            )
        onView(withId(R.id.btnLoginFrag)).perform(click())
        //Verify of forwarding
        onView(withId(R.id.floatingActionButton))
            .check(matches(isDisplayed()))
        onView(withId(R.id.config)).check(matches(isDisplayed()))

    }


    @After
    fun unregisterIdlingResource() {
        onView(withId(R.id.config)).perform(click())
        onView(withId(R.id.btnLogout)).perform(click())
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()
    }

}
