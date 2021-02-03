package elite.kit.outwait.services

class ServiceHandler {

    // True, falls bereits ein Service gestartet wurde
    private var serviceRunning: Boolean = false

    fun startTimerService() {

        if (serviceRunning == false) {
            //TODO starte Service und erzeuge NotifChannel
        }

        serviceRunning = true
    }
}
