package edu.kit.outwait.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import javax.inject.Inject

/**
 * This class starts the timer service providing it with the needed context
 * and ensures that the client repository will not get destroyed during the runtime
 * of the service
 *
 * @property context the needed context for the start of the timer service
 */
class ServiceHandler @Inject constructor(private val context: Context) {

    /** Anonymous reference to the repository */
    var repo: Any? = null

    /**
     * This method starts the timer foreground service and passes an
     * anonymous reference of the repository
     *
     * @param repo the repository that needs to be held alive during the runtime
     * of the foreground service
     */
    fun startTimerService(repo: Any) {
        this.repo = repo
        val serviceIntent = Intent(context, TimerService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
        Log.i("ServiceHandler", "Service started in ServiceHandler")
    }
}
