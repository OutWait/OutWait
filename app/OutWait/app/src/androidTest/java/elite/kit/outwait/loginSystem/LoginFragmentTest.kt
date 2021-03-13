package elite.kit.outwait.loginSystem

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import elite.kit.outwait.MainActivity
import elite.kit.outwait.R
import elite.kit.outwait.utils.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginFragmentTest {


    @get:Rule
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)

    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun loginSuccess() {
        onView(withId(R.id.etInstituteName)).perform(typeText("test1"), closeSoftKeyboard())
        onView(withId(R.id.etInstitutePassword)).perform(typeText("test1"), closeSoftKeyboard())
        onView(withId(R.id.btnLoginFrag)).perform(click())
        Thread.sleep(4000)
        onView(withId(R.id.floatingActionButton)).check(matches(isDisplayed()))
       // openActivityRule.scenario.close()
    }

    /*@Test
    fun loginDenied() {
        onView(withId(R.id.etInstituteName)).perform(typeText("test2"), closeSoftKeyboard())
        onView(withId(R.id.etInstitutePassword)).perform(typeText("test1"), closeSoftKeyboard())
        onView(withId(R.id.btnLoginFrag)).perform(click())
        onView(withId(R.id.managementViewContainer)).check((doesNotExist()))
        openActivityRule.scenario.close()

    }*/

  /*  @Test
    fun addSlot(){
        onView(withId(R.id.etInstituteName)).perform(typeText("test1"), closeSoftKeyboard())
        onView(withId(R.id.etInstitutePassword)).perform(typeText("test1"), closeSoftKeyboard())
        onView(withId(R.id.btnLoginFrag)).perform(click())
        //onView(withId(R.id.managementViewContainer)).check(matches(isDisplayed()))
        Thread.sleep(4000)
        onView(withId(R.id.floatingActionButton)).perform(click())
        Thread.sleep(2000)
        onView(withText("OK")).perform(click())
        openActivityRule.scenario.close()

    }*/


}
