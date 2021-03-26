package elite.kit.outwait.util

import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.activityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import elite.kit.outwait.MainActivity
import elite.kit.outwait.clientDatabase.ClientInfoDao
import elite.kit.outwait.instituteRepository.InstituteRepository
import elite.kit.outwait.utils.EspressoIdlingResource
import elite.kit.outwait.waitingQueue.timeSlotModel.ClientTimeSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class TestTemplate {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var openActivityRule = activityScenarioRule<MainActivity>()

    @Inject
    lateinit var instituteRepo: InstituteRepository

    @Inject
    lateinit var clientInfoDao: ClientInfoDao



    @Before
    fun init() {
        hiltRule.inject()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        establishPreconditions()
    }

    private fun establishPreconditions() {

            // perform login
            instituteRepo.login(
                VALID_TEST_USERNAME,
                VALID_TEST_PASSWORD
            )
            Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

            // check that we are logged in
            assert(instituteRepo.isLoggedIn().value!!)

            // ensure that waiting queue is empty to begin with
            val timeSlots = instituteRepo.getObservableTimeSlotList().value

            if (timeSlots != null && timeSlots.isNotEmpty()) {
                val onlyClientSlots : List<ClientTimeSlot> = timeSlots.filterIsInstance<ClientTimeSlot>()
                for (ClientTimeSlot in onlyClientSlots){
                    // delete slot with retrieved slotCode from waiting queue
                    instituteRepo.deleteSlot(ClientTimeSlot.slotCode)
                    Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
                }
                // save the transaction and the changes made (execute on main thread)
                CoroutineScope(Dispatchers.Main).launch {
                    instituteRepo.saveTransaction()
                }
                Thread.sleep(WAIT_RESPONSE_SERVER_LONG)
            }
    }

    @Test
    fun testMethod(){
        // foo
        // and
        // bar
    }

    @After
    fun tearDown() {

        // logout of management
        CoroutineScope(Dispatchers.Main).launch {
            instituteRepo.logout()
        }
        Thread.sleep(WAIT_RESPONSE_SERVER_LONG)

        // check that we are logged out
        assert(!instituteRepo.isLoggedIn().value!!)

        // clean client DB (so view can navigate back to login fragment)
        clientInfoDao.clearTable()

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        openActivityRule.scenario.close()
    }
}

