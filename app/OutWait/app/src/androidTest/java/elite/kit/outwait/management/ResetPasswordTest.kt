package elite.kit.outwait.management

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import elite.kit.outwait.MainActivity
import elite.kit.outwait.R
import elite.kit.outwait.util.RESET_ACCOUNT_NAME
import elite.kit.outwait.util.StringResource
import elite.kit.outwait.utils.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResetPasswordTest {

    @get:Rule
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    //T5
    @Test
    fun resetPassword(){
        onView(withId(R.id.tvPForgotton)).perform(click())
        onView(withId(R.id.etInstituteNamePasswordForgotten)).perform(typeText(RESET_ACCOUNT_NAME),closeSoftKeyboard())
        onView(withId(R.id.btnPasswordForgot)).perform(click())
        onView(isRoot()).perform(pressBack())
        onView(withId(R.id.tvTitleLogin)).check(matches(isDisplayed()))
    }

    @After
    fun unRegisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)

    }
}
