package edu.kit.outwait.recyclerviewScreens.configurationsScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.kit.outwait.customDataTypes.Mode
import edu.kit.outwait.customDataTypes.Preferences
import edu.kit.outwait.instituteRepository.InstituteRepository
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval
import javax.inject.Inject

/**
 * Keeps all data of the configDialogFragment
 *
 * @property repo Instance of repository for the institute
 * @property coordinator Instance of coordinator
 */
@HiltViewModel
class ConfigDialogViewModel @Inject constructor(
    val repo: InstituteRepository,
    val coordinator: ConfigCoordinator,
) :
    ViewModel() {
    /**
     * Size of current list
     */
    val slotListSize = repo.getObservableTimeSlotList()

    /**
     * Keeps preferences from repository
     */
    val preferences: LiveData<Preferences> = repo.getObservablePreferences()

    /**
     * Sets standard duration of a slot depend on preferences
     */
    var standardSlotDuration: Duration = preferences.value!!.defaultSlotDuration
    /**
     * Sets notification time of a slot depend on preferences
     */
    var notificationTime: Duration = preferences.value!!.notificationTime
    /**
     * Sets mode of queue depend on preferences
     */
    var isModeTwo = preferences.value!!.mode==Mode.TWO
    /**
     * Sets prioritization time  of a slot depend on preferences
     */
    val prioritizationTime: Duration = preferences.value!!.prioritizationTime
    /**
     * Sets delay notification time of a slot depend on preferences
     */
    val delayNotificationTime: Duration = preferences.value!!.delayNotificationTime


    /**
     * Executes logout of a management
     *
     */
    fun logout() {
        repo.logout()
        coordinator.navigateToForwarderFragment()
    }

    /**
     * Transmits entered configurations to repository
     *
     * @param standardSlotDuration Standard duration of a slot
     * @param notificationTime Notification time of a slot
     * @param delayNotificationTime Delay notification time of a slot
     * @param prioritizationTime Prioritization time of a slot
     * @param isModeTwo Mode of queue
     */
    fun saveConfigValues(standardSlotDuration:Duration,
                         notificationTime:Duration,
                         delayNotificationTime:Duration,
                         prioritizationTime:Duration,
                         isModeTwo:Boolean) {
        val newMode = if (isModeTwo) {
            Mode.TWO
        } else {
            Mode.ONE
        }
        repo.changePreferences(Preferences(
            standardSlotDuration,
            notificationTime,
            delayNotificationTime,
            prioritizationTime,
            newMode
        ))
    }
}
