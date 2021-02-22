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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepository @Inject constructor(
    private val dao: ClientInfoDao,
    private val remote: ClientHandler,
    private val serviceHandler: ServiceHandler) {

    init {
        //Get notified with server errors
        remote.getErrors().observeForever {
            if (it.last() == ClientServerErrors.INVALID_SLOT_CODE){
                pushError(ClientErrors.INVALID_SLOT_CODE)
            }
        }
        CoroutineScope(IO).launch {
            dao.clearTable()
        }
    }
    private val activeSlots = dao.getAllClientInfoObservable()// MutableLiveData<List<ClientInfo>>()//
    private val errorNotifications = MutableLiveData<List<ClientErrors>>()

    private var remoteConnected = false

    suspend fun newCodeEntered(code : String?) {
        if (code === null || code == ""){
            pushError(ClientErrors.INVALID_SLOT_CODE)
            return
        }
        serviceHandler.startTimerService()
        withContext(IO){
            Log.d("newCodeEntered::cRepo", "entered code: $code")
            if(remoteConnected || remote.initCommunication()) {
                remoteConnected = true
                remote.newCodeEntered(code)
            }
        }
    }
    fun refreshWaitingTime(code : String){

    }
    fun getActiveSlots() : LiveData<List<ClientInfo>> = activeSlots
    fun getErrorNotifications() = errorNotifications as LiveData<List<ClientErrors>>

    private fun pushError(error: ClientErrors){
        if (errorNotifications.value !== null){
            val newList = errorNotifications.value!!.plus(error).toMutableList()
            errorNotifications.value = newList
        }else{
            errorNotifications.value = listOf(error)
        }
    }
}
