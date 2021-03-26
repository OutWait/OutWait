package elite.kit.outwait


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import elite.kit.outwait.instituteRepository.InstituteRepository
import elite.kit.outwait.util.AppointmentSetter
import elite.kit.outwait.util.StringResource
import elite.kit.outwait.util.VALID_TEST_PASSWORD
import elite.kit.outwait.util.VALID_TEST_USERNAME
import elite.kit.outwait.utils.EspressoIdlingResource
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
class AppointmentSetterTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Inject
    lateinit var instituteRepo: InstituteRepository

    @Before
    fun init() {
        hiltRule.inject()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        instituteRepo.login("test2", "test2")
    }

    @Test
    fun mainActivityTest3() {
        onView(withId(R.id.floatingActionButton)).perform(click())
        onView(withId(R.id.cbIsFixedSlot)).perform(click())
        onView(withId(R.id.tpAppointmentTime)).perform(scrollTo())
        onView(withId(R.id.tpAppointmentTime)).perform(AppointmentSetter.setAppointment(22,22))
        Thread.sleep(6000)
        onView(withText(StringResource.getResourceString(R.string.confirm))).perform(click())
        Thread.sleep(6000)
    }
}
