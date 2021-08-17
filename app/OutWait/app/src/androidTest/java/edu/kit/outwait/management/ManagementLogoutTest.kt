package edu.kit.outwait.management

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import edu.kit.outwait.MainActivity
import edu.kit.outwait.R
import edu.kit.outwait.instituteRepository.InstituteRepository
import edu.kit.outwait.util.VALID_TEST_PASSWORD
import edu.kit.outwait.util.VALID_TEST_USERNAME
import edu.kit.outwait.utils.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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
    fun init() {
        hiltRule.inject()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        //Input of correct login data
        instituteRepository.login(VALID_TEST_USERNAME, VALID_TEST_PASSWORD)
    }

    //T2
    @Test
    fun logoutSuccessfully() {
        //Verify of forwarding
        onView(withId(R.id.floatingActionButton))
            .check(matches(ViewMatchers.isDisplayed()))
        //Click on setting button
        onView(withId(R.id.config)).perform(ViewActions.click())
        //Logout
        onView(withId(R.id.btnLogout)).perform(ViewActions.click())
        //Try again to login
        onView(withId(R.id.btnLoginFrag)).perform(ViewActions.click())
        //Verify on loginFragment
        onView(withId(R.id.tvTitleLogin))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @After
    fun unRegisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()
    }
}
