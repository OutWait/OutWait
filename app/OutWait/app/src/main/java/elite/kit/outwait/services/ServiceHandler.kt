package elite.kit.outwait.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import javax.inject.Inject

class ServiceHandler @Inject constructor(private val context: Context) {

    var repo: Any? = null

    fun startTimerService(repo: Any) {
        this.repo = repo
        val serviceIntent = Intent(context, TimerService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
        Log.i("ServiceHandler", "We returned from startOnCommand")
    }
}
