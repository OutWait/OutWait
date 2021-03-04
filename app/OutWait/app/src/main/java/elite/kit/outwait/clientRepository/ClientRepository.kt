package elite.kit.outwait.clientRepository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elite.kit.outwait.clientDatabase.ClientInfo
import elite.kit.outwait.clientDatabase.ClientInfoDao
import elite.kit.outwait.remoteDataSource.ClientHandler
import elite.kit.outwait.remoteDataSource.ClientServerErrors
import elite.kit.outwait.services.ServiceHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class is the single source of truth for all client-related information
 * for the GUI. It provides the information about the clients running slots from
 * the database and passes a request to the server when a new slot is entered.
 * Note: The Background service holds a reference to the repo, so the garbage
 * collection won´t kill it while the client is waiting and has active slots.
 * This way, we can assure that the few state variables, held by the repo, are correct.
 *
 * @property dao object to access the client info table in the client database
 * @property remote object to send requests to the server
 * @property serviceHandler helper to start the background service
 */
@Singleton
class ClientRepository @Inject constructor(
    private val dao: ClientInfoDao,
    private val remote: ClientHandler,
    private val serviceHandler: ServiceHandler) {

    private val errorNotifications = MutableLiveData<List<ClientErrors>>()

        /**
         * Returns all pushed error notifications in an observable list,
         * sorted chronologically by their point of occurrence.
         */
        fun getErrorNotifications() = errorNotifications as LiveData<List<ClientErrors>>

    /*
    private state variables
     */
    private var remoteConnected = false
    private var serviceStarted = false

    //to access activeSlots more quickly
    private val activeSlots = dao.getAllClientInfoObservable()

        /**
         *
         * Returns an observable list of all the clients waiting slots
         * and their current waiting time prediction.
         *
         * @return observable list of all the clients waiting slots
         * and their current waiting time prediction.
         */
        fun getActiveSlots() : LiveData<List<ClientInfo>> = activeSlots


    init {
        //Get notified with server errors
        remote.getErrors().observeForever {
            if (it.last() == ClientServerErrors.INVALID_SLOT_CODE){
                pushError(ClientErrors.INVALID_SLOT_CODE)
            }
        }


        CoroutineScope(IO).launch {
            withContext(Main){
                activeSlots.observeForever {
                    if (it.isNotEmpty() && !serviceStarted){
                        /*
                        when the first slot the client wants to observe is
                        received, start the service to get updated info in
                        the background
                         */
                        serviceStarted = true
                        serviceHandler.startTimerService(this)
                    } else if (it.isEmpty() && serviceStarted) {
                        /*
                        There is an agreement that the service kills itself
                        when there are no more running slots for the client
                         */
                        serviceStarted = false
                    }
                }
            }
        }
    }

    /**
     * Tell the server that the client has entered a slot code and wants to
     * observe his waiting time
     *
     * @param code wait code for the slot of the client
     */
    suspend fun newCodeEntered(code : String?) {
        if (code === null || code == ""){
            pushError(ClientErrors.INVALID_SLOT_CODE)
            return
        }
        withContext(IO){
            Log.d("newCodeEntered::cRepo", "entered code: $code")
            if(remoteConnected || remote.initCommunication()) {
                remoteConnected = true
                remote.newCodeEntered(code)
            }
        }
    }

    /**
     * requests the server to send us new waiting time explicitly
     * (although it might not have changed)
     *
     * @param code wait code for the slot of the client
     */
    fun refreshWaitingTime(code : String){
        CoroutineScope(IO).launch {
            remote.refreshWaitingTime(code)
        }
    }

    /*
    updates the error notifications list with the passed error in a way that
    observers of the list get notified
     */
    private fun pushError(error: ClientErrors){
        if (errorNotifications.value !== null){
            val newList = errorNotifications.value!!.plus(error).toMutableList()
            errorNotifications.value = newList
        }else{
            errorNotifications.value = listOf(error)
        }
    }
}
