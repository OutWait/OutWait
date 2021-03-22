package elite.kit.outwait.management

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import elite.kit.outwait.MainActivity
import elite.kit.outwait.R
import elite.kit.outwait.instituteRepository.InstituteRepository
import elite.kit.outwait.util.StringResource
import elite.kit.outwait.util.ToastMatcher
import elite.kit.outwait.util.VALID_TEST_PASSWORD
import elite.kit.outwait.util.VALID_TEST_USERNAME
import elite.kit.outwait.utils.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
class ManagementLogoutTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Inject
    lateinit var instituteRepository: InstituteRepository


    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        //Input of correct login data
        hiltRule.inject()
        instituteRepository.login(VALID_TEST_USERNAME, VALID_TEST_PASSWORD)
    }

    //T2
    @Test
    fun logoutSuccessfully(){
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
        onView(ViewMatchers.withText(StringResource.getResourceString(R.string.LOGIN_DENIED))).inRoot(ToastMatcher()).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed())
        )

        onView(withId(R.id.tvTitleLogin))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        openActivityRule.scenario.close()
    }

    @After
    fun unRegisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)

    }
}
