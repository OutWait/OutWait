package elite.kit.outwait.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class TimerService : Service() {

    // Return null as one cannot bind on this services
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * [android.content.Context.startService], providing the arguments it supplied and a
     * unique integer token representing the start request.  Do not call this method directly.
     *
     *
     * For backwards compatibility, the default implementation calls
     * [.onStart] and returns either [.START_STICKY]
     * or [.START_STICKY_COMPATIBILITY].
     *
     *
     * Note that the system calls this on your
     * service's main thread.  A service's main thread is the same
     * thread where UI operations take place for Activities running in the
     * same process.  You should always avoid stalling the main
     * thread's event loop.  When doing long-running operations,
     * network calls, or heavy disk I/O, you should kick off a new
     * thread, or use [android.os.AsyncTask].
     *
     * @param intent The Intent supplied to [android.content.Context.startService],
     * as given.  This may be null if the service is being restarted after
     * its process has gone away, and it had previously returned anything
     * except [.START_STICKY_COMPATIBILITY].
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to
     * start.  Use with [.stopSelfResult].
     *
     * @return The return value indicates what semantics the system should
     * use for the service's current started state.  It may be one of the
     * constants associated with the [.START_CONTINUATION_MASK] bits.
     *
     * @see .stopSelfResult
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
        // TODO: Clean up of holded ressources
    }
}
