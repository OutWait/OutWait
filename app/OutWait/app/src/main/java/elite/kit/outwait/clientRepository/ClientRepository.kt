package elite.kit.outwait.clientRepository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elite.kit.outwait.clientDatabase.ClientInfo
import elite.kit.outwait.clientDatabase.ClientInfoDao
import elite.kit.outwait.remoteDataSource.ClientHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepository @Inject constructor(private val dao: ClientInfoDao, private val remote: ClientHandler) {

    private val activeSlots = MutableLiveData<List<ClientInfo>>()
    private val errorNotifications = MutableLiveData<List<ClientErrors>>()

    fun newCodeEntered(code : String){
        Log.d("newCodeEntered::cRepo", "entered code: $code")
        remote.initCommunication()
    }
    fun refreshWaitingTime(code : String){

    }
    fun getActiveSlots() : LiveData<List<ClientInfo>> = activeSlots
    fun getErrorNotifications() = errorNotifications as LiveData<List<ClientErrors>>
}
