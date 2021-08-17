package edu.kit.outwait.management

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@HiltAndroidTest
class ManagementLoginTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Inject
    lateinit var instituteRepo: InstituteRepository

    @Before
    fun registerIdlingResource() {
        hiltRule.inject()
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
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.logout()
        }
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()
    }

}
