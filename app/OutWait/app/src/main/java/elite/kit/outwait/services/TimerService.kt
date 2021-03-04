package elite.kit.outwait.services

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
import elite.kit.outwait.R
import elite.kit.outwait.channel_1NotificationBuilder
import elite.kit.outwait.channel_2NotificationBuilder
import elite.kit.outwait.clientDatabase.ClientInfo
import elite.kit.outwait.clientDatabase.ClientInfoDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import javax.inject.Inject


@AndroidEntryPoint
class TimerService @Inject constructor(): LifecycleService() {

    @channel_1NotificationBuilder
    @Inject
    lateinit var permNotificationBuilder: NotificationCompat.Builder

    @channel_2NotificationBuilder
    @Inject
    lateinit var secondNotificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var db : ClientInfoDao

    @Inject
    lateinit var handler: ServiceHandler

    private lateinit var allClientInfoAsLiveData: LiveData<List<ClientInfo>>

    private lateinit var nextAppointmentClientInfo: ClientInfo

    private var pendingSlotCodesNotified: MutableList<String> = mutableListOf()

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    override fun onCreate() {
        super.onCreate()

        // TODO NotifChannel konfigurieren (Sound, Buzz etc)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val serviceChannel1 = NotificationChannel(
                PERM_CHANNEL_ID,
                PERM_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            )
            serviceChannel1.description = PERM_CHANNEL_DESCRIPTION

            //TODO macht das einen Unterschied?
            //val manager = getSystemService(NotificationManager::class.java) //-> CodingInFlow
            manager.createNotificationChannel(serviceChannel1)                 //-> RunningApp
            Log.i( "TimerService", "Permanent notifChannel was created")
       // }

      //  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel2 = NotificationChannel(
                SECOND_CHANNEL_ID,
                SECOND_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            serviceChannel2.description = SECOND_CHANNEL_DESCRIPTION
            //val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel2)
            Log.i("TimerService", "Second NotifChannel was created")
        }

        CoroutineScope(Dispatchers.IO).launch {
            if(db.getAllClientInfo().isEmpty()) {
                Log.i("TimerService", "DB WAS empty?!")
            } else {
                val currEnt = db.getAllClientInfo().first()
                Log.i("TimerService", "DB WAS NOT empty?!")
                Log.i("TimerService", "Entry: "+"code: "+ currEnt.slotCode+", instiName: "+currEnt.institutionName)

            }
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val permNotification = permNotificationBuilder.build()
        Log.i("TimerService", " default permNotification was build")
        startForeground(PERM_NOTIFICATION_ID, permNotification)
        Log.i("TimerService", "startForegroundService called")

        //TODO Background Work auf Nebenthread /susp function, alternativ nur LD observen mit
        // AlarmManager oder Workmanager (-> recurring task um imminentAppointments zu checken)

        CoroutineScope(Dispatchers.Main).launch {
            doWork()
        }

        CoroutineScope(Dispatchers.IO).launch {
            while(db.getAllClientInfo().isNotEmpty()) {
                checkNotifiedForExpiredSlotCodes(db.getAllClientInfo())
                checkForPendingAppointment(db.getAllClientInfo())
                Thread.sleep(50000L)
            }
            Log.i("TimerService","DB empty in IODispatch -> Service should stop")
            stopSelf()
        }

        Log.i("TimerService", "super.onStartCommand was returned")
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

    private fun doWork() {
        Log.i("TimerService", "backgroundWork was started")

        allClientInfoAsLiveData = db.getAllClientInfoObservable()
        Log.i("TimerService", "db was accessed")

/*
        allClientInfoAsLiveData.observe(this, Observer { newList ->
            Log.i("TimerService", "LiveData changed")
            if (newList.isEmpty()) {
                Log.i("TimerService", "LiveData empty-> Service should stop")
                stopSelf() // TODO Service richtig beenden, Notif löschen
            } else {
                this.nextAppointmentClientInfo = getNextClientInfo(newList)
            }
            checkNotifiedForExpiredSlotCodes(newList)
            updatePermanentNotification(newList)
            checkForDelay(newList)
            checkForPendingAppointment(newList)
        })

 */


        allClientInfoAsLiveData.observe(this, Observer { newList ->
            Log.i("TimerService", "LiveData changed")
            if (newList.isNotEmpty()) {
                this.nextAppointmentClientInfo = getNextClientInfo(newList)
                checkNotifiedForExpiredSlotCodes(newList)
                updatePermanentNotification(newList)
                checkForDelay(newList)
                checkForPendingAppointment(newList)
            } else {
                Log.i("TimerService", "LiveData empty in doWork() -> Service should stop")
                stopSelf() // TODO Service richtig beenden, Notif löschen
            }

        })


    }

    private fun updatePermanentNotification(allClientInfoList: List<ClientInfo>) {
        val newNextClientInfo = getNextClientInfo(allClientInfoList)
        val instituteName = newNextClientInfo.institutionName
        val appointmentTime = newNextClientInfo.approximatedTime

        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
        val appointmentString = formatter.print(appointmentTime) + " o'clock"


        val notification: Notification = permNotificationBuilder
            .setContentTitle(getString(R.string.Perm_Notif_BaseTitle))
            .setContentText(getString(R.string.Perm_Notif_Basetext1)
                + instituteName
                + getString(R.string.Perm_Notif_Basetext2)
                + appointmentString)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(PERM_NOTIFICATION_ID, notification)
    }

    private fun checkForDelay(allClientInfoList : List<ClientInfo>) {

        val iterator = allClientInfoList.iterator()

        while (iterator.hasNext()) {
            val next = iterator.next()

            if(next.approximatedTime.millis - next.originalAppointmentTime.millis
                 > next.delayNotificationTime.millis) {

               // erzeuge auf secondChannel eine DelayNotification push Notification
                val approxTime = next.approximatedTime
                val delayDuration = Duration(next.approximatedTime.millis - next.originalAppointmentTime.millis)

                val formatter: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
                val appointmentString = formatter.print(approxTime) + " o'clock"
                val delayString = delayDuration.toStandardMinutes().toString() + " min"

                val delayNotification: Notification = secondNotificationBuilder
                    .setContentTitle(getString(R.string.Delay_Notif_BaseTitle) + next.institutionName)
                    .setContentText(getString(R.string.Delay_Notif_BaseText1)
                        + delayString
                        + getString(R.string.Delay_Notif_BaseText2)
                        + appointmentString)
                    .build()

                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(DELAY_NOTIFICATION_ID, delayNotification)


                // Setze originalTime auf aktuell approxTime für zukünftige Verspätungsüberprüfung
                //TODO Da DB access darf das nicht auf main thread erfolgen!
                CoroutineScope(Dispatchers.IO).launch {
                    val updatedClientInfo = ClientInfo(
                        next.slotCode, next.institutionName, next.approximatedTime,
                        next.approximatedTime, next.notificationTime, next.delayNotificationTime
                    )
                    db.update(updatedClientInfo)
                }
            }

            // Falls verspätete Slot pending war, setze sein NotifiedFlag zurück für wenn er wieder pending wird
            if (pendingSlotCodesNotified.contains(next.slotCode)) pendingSlotCodesNotified.remove(next.slotCode)
        }

    }

    //TODO Second Notification erzeugen mit Benachrichtung über kurz bevorstehenden Termin
    private fun checkForPendingAppointment(allClientInfoList : List<ClientInfo>) {
        val iterator = allClientInfoList.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()

            if ((next.approximatedTime.millis - DateTime().millis < next.notificationTime.millis)
                && !pendingSlotCodesNotified.contains(next.slotCode)) {
               // create pending Notification for Benachrichtigungszeit push notification
                val pendingNotification: Notification = secondNotificationBuilder
                    .setContentTitle(getString(R.string.Pending_Notif_BaseTitle1)
                        + next.institutionName
                        + getString(R.string.Pending_Notif_BaseTitle2))
                    .setContentText(getString(R.string.Pending_Notif_BaseText))
                    .build()

                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(PENDING_NOTIFICATION_ID, pendingNotification)

                pendingSlotCodesNotified.add(next.slotCode)
            }

        }
    }

    private fun getNextClientInfo(allClientInfoList: List<ClientInfo>) : ClientInfo {
        var nextClientInfo = allClientInfoList.first()

        val iterator = allClientInfoList.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.approximatedTime.isBefore(nextClientInfo.approximatedTime)) nextClientInfo = next
        }
        return nextClientInfo
    }

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
