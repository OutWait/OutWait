package edu.kit.outwait.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import edu.kit.outwait.R
import edu.kit.outwait.channel_1NotificationBuilder
import edu.kit.outwait.channel_2NotificationBuilder
import edu.kit.outwait.clientDatabase.ClientInfo
import edu.kit.outwait.clientDatabase.ClientInfoDao
import edu.kit.outwait.notifManager
import edu.kit.outwait.utils.TransformationOutput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.Duration
import javax.inject.Inject

/**
 * Granularity of the time steps with which the client database is checked for pending appointments
 */
private const val TIME_STEP_FOR_PENDING_CHECK = 30000L

/**
 * The timer service is started, once a slot was entered into the client database and stops itself
 * once the last slot was deleted from the client database. It is responsible for background work,
 * and pushes notification to the user informing about incoming changes regarding his appointments,
 * even when the app is in the background.
 *
 */
@AndroidEntryPoint
class TimerService @Inject constructor(): LifecycleService() {

    @notifManager
    @Inject
    lateinit var injectedManager: NotifManager

    lateinit var manager: NotificationManager

    /**
     * Injected NotificationBuilder for the permanent notification(s) of the foreground service
     */
    @channel_1NotificationBuilder
    @Inject
    lateinit var permNotificationBuilder: NotificationCompat.Builder

    /**
     * Injected NotificationBuilder for the non-permanent notification(s) of the foreground service
     */
    @channel_2NotificationBuilder
    @Inject
    lateinit var secondNotificationBuilder: NotificationCompat.Builder

    /**
     * Injected access to the client database
     */
    @Inject
    lateinit var db : ClientInfoDao

    /**
     * Injected reference to the service handler that started the service
     */
    @Inject
    lateinit var handler: ServiceHandler

    /**
     * Observed LiveData from the client database
     */
    private lateinit var allClientInfoAsLiveData: LiveData<List<ClientInfo>>

    /**
     * The ClientInfo object of the currently next slot in the client database
     */
    private lateinit var nextAppointmentClientInfo: ClientInfo

    /**
     * This list stores slot codes of pending appointments for which the user was already notified
     */
    private var pendingSlotCodesNotified: MutableList<String> = mutableListOf()

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    override fun onCreate() {
        super.onCreate()

        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel1 = NotificationChannel(
                PERM_CHANNEL_ID,
                PERM_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW)
            serviceChannel1.description = PERM_CHANNEL_DESCRIPTION
            manager.createNotificationChannel(serviceChannel1)

            val serviceChannel2 = NotificationChannel(
                SECOND_CHANNEL_ID,
                SECOND_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH)
            serviceChannel2.description = SECOND_CHANNEL_DESCRIPTION
            manager.createNotificationChannel(serviceChannel2)
        }
        Log.i("TimerService", "NotifChannels created")

    }

    /**
     * Called when the service was started (after onCreate) (@see android documentation for further
     * information).
     * This method actually starts the service and its background work / logic.
     *
     * @param intent The Intent supplied to Context.startService(Intent), as given
     * @param flags Additional data about this start request
     * @param startId A unique integer representing this specific request to start
     * @return indicates what semantics the system should use for the service's current started state
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val permNotification = permNotificationBuilder.build()
        startForeground(PERM_NOTIFICATION_ID, permNotification)
        Log.i("TimerService", "startForegroundService called")

        CoroutineScope(Dispatchers.Main).launch {
            doWork()
        }

        // check for pending appointments, independent of LiveData changes
        CoroutineScope(Dispatchers.IO).launch {
            while(db.getAllClientInfo().isNotEmpty()) {
                delay(TIME_STEP_FOR_PENDING_CHECK)
                checkNotifiedForExpiredSlotCodes(db.getAllClientInfo())
                checkForPendingAppointment(db.getAllClientInfo())
            }
            Log.i("TimerService","DB empty, Service should stop")
            // cancel all remaining (non permanent) notifications
            manager.cancelAll()
            stopSelf()
        }

        Log.i("TimerService", "super.onStartCommand was called")
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.i("TimerService", "Service onDestroy was called")
    }

    /**
     * This method observers the client database via LiveData and reacts to changes with
     * pushing respective notifications to the user or stopping the service
     *
     */
    private fun doWork() {
        Log.i("TimerService", "backgroundWork was started")
        allClientInfoAsLiveData = db.getAllClientInfoObservable()
        Log.i("TimerService", "db was accessed")

        allClientInfoAsLiveData.observe(this, Observer { newList ->
            Log.i("TimerService", "LiveData changed")
            if (newList.isNotEmpty()) {
                this.nextAppointmentClientInfo = getNextClientInfo(newList)
                checkNotifiedForExpiredSlotCodes(newList)
                updatePermanentNotification(newList)
                checkForDelay(newList)
                checkForPendingAppointment(newList)
            } else {
                Log.i("TimerService/doWork()", "LiveData empty, Service should stop")
                // cancel all remaining (non permanent) notifications
                manager.cancelAll()
                stopSelf()
            }
        })
    }

    /**
     * Updates the permanent notification with information regarding the current next
     * slot / appointment
     *
     * @param allClientInfoList all ClientInfos currently in the client database
     */
    private fun updatePermanentNotification(allClientInfoList: List<ClientInfo>) {
        val newNextClientInfo = getNextClientInfo(allClientInfoList)
        val instituteName = newNextClientInfo.institutionName
        val appointmentTime = newNextClientInfo.approximatedTime

        val appointmentString = TransformationOutput.appointmentToString(appointmentTime)

        val notification: Notification = permNotificationBuilder
            .setContentTitle(getString(R.string.Perm_Notif_BaseTitle))
            .setContentText(getString(R.string.Perm_Notif_Basetext1)
                + instituteName
                + getString(R.string.Perm_Notif_Basetext2)
                + appointmentString
                + getString(R.string.Notfi_Basetext_oClock))
            .build()

        // update the permanent notification
        injectedManager.notify(PERM_NOTIFICATION_ID, notification)
    }

    /**
     * Checks the current slots in the client database for delays, meaning that the difference between
     * their currently approximated and original time is greater than their current delayNotificationTime.
     * If that is the case, an appropriate push notification is created and the original time is reset allowing
     * to check for further delays in the future.
     *
     * @param allClientInfoList all ClientInfos currently in the client database
     */
    private fun checkForDelay(allClientInfoList : List<ClientInfo>) {

        val iterator = allClientInfoList.iterator()

        while (iterator.hasNext()) {
            val next = iterator.next()

            if(Duration(next.originalAppointmentTime, next.approximatedTime)
                >= next.delayNotificationTime) {

               // create delay push notification on the second notification channel
                val approxTime = next.approximatedTime
                val delayDuration = Duration(next.approximatedTime.millis - next.originalAppointmentTime.millis)

                val appointmentString = TransformationOutput.appointmentToString(approxTime)

                val delayString = TransformationOutput.durationToString(delayDuration)

                val delayNotification: Notification = secondNotificationBuilder
                    .setContentTitle(getString(R.string.Delay_Notif_BaseTitle) + next.institutionName)
                    .setContentText(getString(R.string.Delay_Notif_BaseText1)
                        + delayString
                        + getString(R.string.Delay_Notif_BaseText2)
                        + appointmentString
                        + getString(R.string.Notfi_Basetext_oClock))
                    .build()

                // cancel old pending appointment notification if existing
                manager.cancel(PENDING_NOTIFICATION_ID)
                // update delay notification (replacing old delay notification if existing)
                injectedManager.notify(DELAY_NOTIFICATION_ID, delayNotification)

                // reset original time to current approx, for new delay checks
                CoroutineScope(Dispatchers.IO).launch {
                    val updatedClientInfo = ClientInfo(
                        next.slotCode, next.institutionName, next.approximatedTime,
                        next.approximatedTime, next.notificationTime, next.delayNotificationTime
                    )
                    db.update(updatedClientInfo)
                }
                // if delayed appointment was pending and notified, reset to not pending resp. not notified
                if (this.pendingSlotCodesNotified.contains(next.slotCode)) pendingSlotCodesNotified.remove(next.slotCode)
            }
        }
    }

    /**
     * Checks the current slots in the client database, if any of them is pending meaning the difference
     * between the currently approximated time and the current time is smaller than their current
     * notification time. If thats the case an appropriate push notification is created and the
     * respective slot (code) is marked as already notified
     *
     * @param allClientInfoList all ClientInfos currently in the client database
     */
    private fun checkForPendingAppointment(allClientInfoList : List<ClientInfo>) {
        val iterator = allClientInfoList.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            val nextRemainingTimeInMillis = Duration(DateTime.now(), next.approximatedTime)

            // if appointment is pending and was not notified already
            if ((nextRemainingTimeInMillis < next.notificationTime)
                && !pendingSlotCodesNotified.contains(next.slotCode)) {

                // create push notification for pending appointment
                val pendingNotification: Notification = secondNotificationBuilder
                    .setContentTitle(getString(R.string.Pending_Notif_BaseTitle1)
                        + next.institutionName
                        + getString(R.string.Pending_Notif_BaseTitle2))
                    .setContentText(getString(R.string.Pending_Notif_BaseText))
                    .build()

                // cancel old delay notification if existing
                manager.cancel(DELAY_NOTIFICATION_ID)
                // update pending appointment notification (replacing old pending appointment notif if existing)
                injectedManager.notify(PENDING_NOTIFICATION_ID, pendingNotification)

                pendingSlotCodesNotified.add(next.slotCode)
            }
        }
    }

    /**
     * Gets the ClientInfo of the appointment that is currently the next one
     *
     * @param allClientInfoList all ClientInfos currently in the client database
     * @return the ClientInfo of the slot that is currently the next one
     */
    private fun getNextClientInfo(allClientInfoList: List<ClientInfo>) : ClientInfo {
        var nextClientInfo = allClientInfoList.first()

        val iterator = allClientInfoList.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.approximatedTime.isBefore(nextClientInfo.approximatedTime)) nextClientInfo = next
        }
        return nextClientInfo
    }

    /**
     * Checks the list of already notified slot codes for expired slot codes, meaning these slot codes
     * are no longer stored in the client database (or rather contained in the LiveData)
     *
     * @param allClientInfoList all ClientInfos currently in the client database
     */
    private fun checkNotifiedForExpiredSlotCodes(allClientInfoList : List<ClientInfo>) {
        for (notified in pendingSlotCodesNotified) {
            var isExpired = true

            for (clientInfo in allClientInfoList) {
                if (clientInfo.slotCode == notified) {
                    isExpired = false
                }
            }
            if (isExpired) pendingSlotCodesNotified.remove(notified)
        }
    }
}
