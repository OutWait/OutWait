package elite.kit.outwait.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class ServiceHandler {

    fun startTimerService(context: Context) {
        val serviceIntent = Intent(context, TimerService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
        Log.i("ServiceHandler", "We returned from startOnCommand")
    }
}
