package elite.kit.outwait.services

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import elite.kit.outwait.MainActivity
import elite.kit.outwait.R
import android.app.*
import android.os.Build
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import elite.kit.outwait.clientDatabase.ClientInfo
import elite.kit.outwait.clientDatabase.ClientInfoDao
import org.joda.time.DateTime
import javax.inject.Inject


// TODO: Wie Zugriff oder Referenz aufs Repo halten?
@AndroidEntryPoint
class TimerService @Inject constructor(): LifecycleService() {

    @Inject
    lateinit var db : ClientInfoDao

    @Inject
    lateinit var handler: ServiceHandler

    private lateinit var allClientInfoAsLiveData: LiveData<List<ClientInfo>> //db.getAllClientInfoObservable()

    private lateinit var nextAppointmentClientInfo: ClientInfo

    private var pendingSlotCodesNotified: MutableList<String> = mutableListOf()

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    override fun onCreate() {
        super.onCreate()

        // TODO NotifChannel konfigurieren (Sound, Buzz etc)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel1 = NotificationChannel(
                getString(R.string.permanentChannel),
                "Example Service Channel 1",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel1)
        }
        Log.i( "TimerService", "Permanent notifChannel was created")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel2 = NotificationChannel(
                getString(R.string.secondChannel),
                "Example Service Channel 2",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel2)
        }
        Log.i("ForegroundService", "Second NotifChannel was created")
        db.getAllClientInfoObservable()
        Log.i("TimerService", "Second NotifChannel was created")


    }

    override
    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Pending Intent für die Notification
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
            0, notificationIntent, 0)

        // Baue die (permanente) Notification für den TimerService
        val notification: Notification = NotificationCompat.Builder(this, getString(R.string.permanentChannel))
            .setContentTitle("Example Service")
            .setContentText("example text")
            .setSmallIcon(R.drawable.ic_timer)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
        Log.i("TimerService", "service was started")

        //TODO Background Work auf Nebenthread /susp function
        //here: do "heavy work" on a background thread
        //doWork()
        //stopSelf()

        //TODO mit was returnen sticky oder redeliver_intent?
        // mit was returned super.onStartCommand?
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
        // TODO >>>>>>>>>>>>AUF DB KANN NOCH NICHT ZUGEGRIFFEN WERDEN <<<<<<<<<<<<<<<<
        allClientInfoAsLiveData = db.getAllClientInfoObservable()
        Log.i("TimerService", "db was accessed")
        // TODO >>>>>>>>>>>>AUF DB KANN NOCH NICHT ZUGEGRIFFEN WERDEN <<<<<<<<<<<<<<<<

        val allClientInfo = allClientInfoAsLiveData.value
        if (allClientInfo == null || allClientInfo.isEmpty()) {
            stopSelf() // TODO stopSelf sauber machen
        } else {
            this.nextAppointmentClientInfo = getNextClientInfo(allClientInfo)
        }


        allClientInfoAsLiveData.observe(this, Observer { newList ->
            Log.i("TimerService", "LiveData changed")
            if (newList.isEmpty()) stopSelf()
            // TODO Service richtig beenden, Notif löschen
            checkNotifiedForExpiredSlotCodes(newList)
            updateNotification(newList)
            checkForDelay(newList)
            checkForPendingAppointment(newList)

        })

        // TODO While loop mit LiveData observen um Notification upzudaten (a la Timer?)


        // TODO("DB regelmäßig überprüfen um auf immamentAppointment zu checken")
        // TODO debugge um zu prüfen ob & wann die clientDB null oder nulllables returned (returnen kann)
        while (db.getAllClientInfo().isNotEmpty() || db.getAllClientInfo() != null) {
            checkForPendingAppointment(db.getAllClientInfo())
            checkNotifiedForExpiredSlotCodes(db.getAllClientInfo())
            // sleep for 5min
            Thread.sleep(300000)
        }

        stopSelf() //TODO stopSelf sauber machen

    }

    private fun updateNotification(allClientInfoList : List<ClientInfo>) {
        val newNextClientInfo = getNextClientInfo(allClientInfoList)
        val newNextAppointmentTime = newNextClientInfo.approximatedTime

        TODO ("Permanent Notification updaten mit aktueller Zeit des nächsten Slots")
    }

    private fun checkForDelay(allClientInfoList : List<ClientInfo>) {

        val iterator = allClientInfoList.iterator()

        while (iterator.hasNext()) {
            val next = iterator.next()

            if(next.approximatedTime.millis - next.originalAppointmentTime.millis
                 > next.delayNotificationTime.millis) {

               // TODO("erzeuge secondNotif mit Verspätungsbenachrichtigung")

                // Setze originalTime auf aktuell approxTime für zukünftige Verspätungsüberprüfung
                val updatedClientInfo = ClientInfo(
                    next.slotCode, next.institutionName, next.approximatedTime,
                    next.approximatedTime, next.notificationTime, next.delayNotificationTime)
                db.update(updatedClientInfo)
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
               //  TODO("erzeuge secondNotification für kurzbevorstehenden Termin")
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
