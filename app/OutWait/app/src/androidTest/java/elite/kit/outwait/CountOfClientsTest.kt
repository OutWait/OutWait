package elite.kit.outwait

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText

import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import elite.kit.outwait.dataItem.TimeSlotItem
import elite.kit.outwait.instituteRepository.InstituteRepository
import elite.kit.outwait.recyclerviewSetUp.viewHolder.BaseViewHolder
import elite.kit.outwait.util.StringResource
import elite.kit.outwait.util.validPassword
import elite.kit.outwait.util.validUsername
import elite.kit.outwait.utils.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

private const val INSTITUTION_NAME_CORRECT = "test2"
private const val INSTITUTION_PASSWORD_CORRECT = "test2"
private const val SLOT_IDENTIFIER_ONE = "First person"
private const val SLOT_IDENTIFIER_TWO = "Second person"
private const val SLOT_IDENTIFIER_THREE = "Third person"
private const val FIRST_SLOT     = 0
private const val FIRST_SLOT_TRANSACTION     = 1



private const val THREE = "3"

//@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class CountOfClientsTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Inject
    lateinit var instituteRepo: InstituteRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Before
    fun addSlots() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)

        // login via injected repository
        instituteRepo.login(
            validUsername,
            validPassword
        )
        /*
        //Login
        onView(withId(R.id.etInstituteName))
            .perform(
                typeText(INSTITUTION_NAME_CORRECT),
                closeSoftKeyboard()
            )
        onView(withId(R.id.etInstitutePassword))
            .perform(
                typeText(INSTITUTION_PASSWORD_CORRECT),
                closeSoftKeyboard()
            )
        onView(withId(R.id.btnLoginFrag)).perform(click())

         */
        //Verify of forwarding
        onView(withId(R.id.floatingActionButton)).perform(click())

        //Add first slot
        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(SLOT_IDENTIFIER_ONE), closeSoftKeyboard())
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())

        //Add second slot
        onView(withId(R.id.floatingActionButton)).perform(click())

        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(SLOT_IDENTIFIER_TWO),closeSoftKeyboard())
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())

        //Add third slot
        onView(withId(R.id.floatingActionButton)).perform(click())

        onView(withId(R.id.etIdentifierAddDialog))
            .perform(typeText(SLOT_IDENTIFIER_THREE), closeSoftKeyboard())
        onView(withText(StringResource.getResourceString(R.string.confirm)))
            .perform(click())


        onView(withId(R.id.ivSaveTransaction)).perform(click())

    }

    //T30
    @Test
    fun areThreeClientsAdded() {
        onView(withId(R.id.config)).perform(click())
        onView(withId(R.id.countOfClients)).check(matches(withText(StringResource.getResourceString(R.string.text_counter) + THREE)))
    }

    @After
    fun emptyQueue() {
        onView(ViewMatchers.isRoot()).perform(pressBack())

        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT,
                swipeLeft()
            )
        )

        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT_TRANSACTION,
                swipeLeft()
            )
        )

        onView(withId(R.id.slotList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<BaseViewHolder<TimeSlotItem>>(
                FIRST_SLOT_TRANSACTION,
                swipeLeft()
            )
        )
        Thread.sleep(100)
        onView(withId(R.id.ivSaveTransaction)).perform(click())
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()

    }



}
