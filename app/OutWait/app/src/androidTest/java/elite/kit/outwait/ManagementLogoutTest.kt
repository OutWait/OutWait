package elite.kit.outwait

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import elite.kit.outwait.utils.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val INSTITUTION_NAME_CORRECT="test1"
private const val INSTITUTION_PASSWORD_CORRECT="test1"
@RunWith(AndroidJUnit4::class)
class ManagementLogoutTest {

    @get:Rule
    var openActivityRule = activityScenarioRule<MainActivity>()


    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    //T2
    @Test
    fun logoutSuccessfully(){
        //Input of correct login data
        onView(ViewMatchers.withId(R.id.etInstituteName))
            .perform(ViewActions.typeText(INSTITUTION_NAME_CORRECT), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.etInstitutePassword))
            .perform(ViewActions.typeText(INSTITUTION_PASSWORD_CORRECT), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.btnLoginFrag)).perform(ViewActions.click())
        //Verify of forwarding
        onView(withId(R.id.floatingActionButton))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        //Click on setting button
        onView(withId(R.id.config)).perform(ViewActions.click())
        //Logout
        onView(withId(R.id.btnLogout)).perform(ViewActions.click())
        //Try again to login
        onView(withId(R.id.btnLoginFrag)).perform(ViewActions.click())
        //Verify on loginFragment
        onView(withId(R.id.tvTitleLogin))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        openActivityRule.scenario.close()
    }

    @After
    fun unRegisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)

    }
}