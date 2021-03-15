package elite.kit.outwait

import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.*
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import elite.kit.outwait.util.ReadText
import elite.kit.outwait.util.StringResource
import elite.kit.outwait.utils.EspressoIdlingResource
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.Description
import org.junit.runner.RunWith
import java.util.regex.Matcher


private const val INSTITUTION_NAME_CORRECT = "test2"
private const val INSTITUTION_PASSWORD_CORRECT = "test2"
private const val SLOT_IDENTIFIER_ONE = "Slot1"

@RunWith(AndroidJUnit4::class)
class IdlingResourceTest {


    @Before
    fun register() {
       IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun login() {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        //Input of correct login data



        Espresso.onView(ViewMatchers.withId(R.id.etInstituteName))
            .perform(
                ViewActions.typeText(INSTITUTION_NAME_CORRECT),
                ViewActions.closeSoftKeyboard()
            )
        Espresso.onView(ViewMatchers.withId(R.id.etInstitutePassword))
            .perform(
                ViewActions.typeText(INSTITUTION_PASSWORD_CORRECT),
                ViewActions.closeSoftKeyboard()
            )
        Espresso.onView(ViewMatchers.withId(R.id.btnLoginFrag)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.floatingActionButton)).check(matches(isDisplayed()))


        Espresso.onView(ViewMatchers.withId(R.id.floatingActionButton)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.etIdentifierAddDialog))
            .perform(ViewActions.typeText(SLOT_IDENTIFIER_ONE), ViewActions.closeSoftKeyboard())


        Espresso.onView(ViewMatchers.withText(StringResource.getResourceString(R.string.confirm)))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.ivSaveTransaction)).perform(ViewActions.click())


        activityScenario.close()
    }

    @After
    fun unregister() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)

    }




}
