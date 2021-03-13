package elite.kit.outwait

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val INSTITUTION_NAME_CORRECT="test1"
private const val INSTITUTION_PASSWORD_CORRECT="test1"
private const val INSTITUTION_PASSWORD_WRONG="test2"
@RunWith(AndroidJUnit4::class)
class ManagementLoginTest {

    @get:Rule
    var openActivityRule = activityScenarioRule<MainActivity>()

    //T1
    @Test
    fun loginSuccessfully(){
        //Input of correct login data
        Espresso.onView(ViewMatchers.withId(R.id.etInstituteName))
            .perform(ViewActions.typeText(INSTITUTION_NAME_CORRECT), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.etInstitutePassword))
            .perform(ViewActions.typeText(INSTITUTION_PASSWORD_CORRECT), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.btnLoginFrag)).perform(ViewActions.click())
        Thread.sleep(4000)
        //Verify of forwarding
        Espresso.onView(ViewMatchers.withId(R.id.floatingActionButton))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.config)).check(matches(isDisplayed()))
        openActivityRule.scenario.close()
    }

    //Extra Test
    @Test
    fun loginDenied(){
        //Input of wrong login data
        Espresso.onView(ViewMatchers.withId(R.id.etInstituteName))
            .perform(ViewActions.typeText(INSTITUTION_NAME_CORRECT), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.etInstitutePassword))
            .perform(ViewActions.typeText(INSTITUTION_PASSWORD_WRONG), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.btnLoginFrag)).perform(ViewActions.click())
        Thread.sleep(4000)
        //Verify on loginFragment
        Espresso.onView(ViewMatchers.withId(R.id.tvTitleLogin))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        openActivityRule.scenario.close()
    }
}
