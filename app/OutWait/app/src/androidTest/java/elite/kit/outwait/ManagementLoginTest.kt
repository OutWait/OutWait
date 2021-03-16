package elite.kit.outwait

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
import elite.kit.outwait.utils.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val INSTITUTION_NAME_CORRECT = "test1"
private const val INSTITUTION_PASSWORD_CORRECT = "test1"
private const val INSTITUTION_PASSWORD_WRONG = "Test2"


@RunWith(AndroidJUnit4::class)
class ManagementLoginTest {

    @get:Rule
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
                ViewActions.typeText(INSTITUTION_NAME_CORRECT),
                ViewActions.closeSoftKeyboard()
            )
        onView(withId(R.id.etInstitutePassword))
            .perform(
                ViewActions.typeText(INSTITUTION_PASSWORD_CORRECT),
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
