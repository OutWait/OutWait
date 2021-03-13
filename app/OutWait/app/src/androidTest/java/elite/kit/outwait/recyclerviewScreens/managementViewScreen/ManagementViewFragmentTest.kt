package elite.kit.outwait.recyclerviewScreens.managementViewScreen

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import elite.kit.outwait.MainActivity
import elite.kit.outwait.R
import kotlinx.android.synthetic.main.management_view_fragment.view.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ManagementViewFragmentTest{
    @get:Rule
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Test
    fun test(){
        onView(withId(R.id.managementViewContainer)).check(matches(isDisplayed()))
    }
}
