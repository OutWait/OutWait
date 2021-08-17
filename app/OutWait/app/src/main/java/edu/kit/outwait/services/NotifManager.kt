package edu.kit.outwait.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import javax.inject.Inject

class NotifManager @Inject constructor(private val context: Context) {

    lateinit var sysManager: NotificationManager

    fun createNotificationChannel(channel: NotificationChannel) {
        sysManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sysManager.createNotificationChannel(channel)
        }
    }

    fun cancelAll() {
        sysManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        sysManager.cancelAll()
    }

    fun notify(id: Int, notification: Notification) {
        sysManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        sysManager.notify(id, notification)
    }

    fun cancel(id: Int) {
        sysManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        sysManager.cancel(id)
    }
}
