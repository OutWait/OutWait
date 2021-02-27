package elite.kit.outwait.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import elite.kit.outwait.MainActivity
import elite.kit.outwait.R
import android.app.*
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import elite.kit.outwait.clientDatabase.ClientInfo
import elite.kit.outwait.clientDatabase.ClientInfoDao
import javax.inject.Inject
import javax.inject.Singleton


// TODO: Wie Zugriff oder Referenz aufs Repo halten?
class TimerService @Inject constructor(): Service() {

    @Inject
    lateinit var db : ClientInfoDao

    @Inject
    lateinit var handler: ServiceHandler

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
        Log.i( "ForegroundService", "Permanent notifChannel was created")

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


    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        // Pending Intent für die Notification
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
            0, notificationIntent, 0)

        // Baue die (permanente) Notification für den ForegroundService
        val notification: Notification = NotificationCompat.Builder(this, getString(R.string.permanentChannel))
            .setContentTitle("Example Service")
            .setContentText("example text")
            .setSmallIcon(R.drawable.ic_timer)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
        Log.i("ForegroundService", "service was started")

        //TODO Background Work auf Nebenthread /susp function
        //here: do "heavy work" on a background thread
        //doWork()
        //TODO loop mit Abfrage wann selbst gestoppt werden soll (Alternativ in workMethode?)
        stopSelf()

        //TODO mit was returnen sticky oder redeliver_intent?
        return START_REDELIVER_INTENT
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.i("ForegroundService", "Service onDestroy was called")
    }

    private fun doWork() {
        Log.i("ForegroundService", "backkgroundWork was started")

        // TODO Aktuellsten Termin bzw ClientInfo aus DB holen, bereits sortiert?
        // TODO Observer auf LiveData Änderungen
        val allClientInfoAsLiveData = db.getAllClientInfoObservable()
        val allDBEntries = allClientInfoAsLiveData.value
        if (allDBEntries == null) stopSelf()

        var nextClientInfo: ClientInfo = getNextClientInfo(allDBEntries!!)

        val nextAppointmentTime = nextClientInfo.approximatedTime
        val nextOriginalTime = nextClientInfo.originalAppointmentTime

        // TODO String Resources in xml für Notif Text, dann hier joinen mit aktuellen Daten

        //TODO notification erzeugen mit aktuellen Daten auf Channel 2

        // TODO While loop mit LiveData observen um Notification upzudaten

        /*
        //TODO Muss um LiveData zu oberserven das Lifecycle Interface implementiert werden?
        allClientInfoAsLiveData.observe(this, Observer { data ->
            nextClientInfo = getNextClientInfo(data)
            // TODO notif updaten (in extra methode?)
        } )
         */
    }

    private fun getNextClientInfo(allClientInfo: List<ClientInfo>) : ClientInfo {
        var nextClientInfo = allClientInfo.first()
        val iterator = allClientInfo.iterator()

        while (iterator.hasNext()) {
            val next = iterator.next()

            if (nextClientInfo.approximatedTime > next.approximatedTime) nextClientInfo = next
        }
        return nextClientInfo
    }
}
