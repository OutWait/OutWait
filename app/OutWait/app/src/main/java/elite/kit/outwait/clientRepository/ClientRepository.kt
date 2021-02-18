package elite.kit.outwait.clientRepository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elite.kit.outwait.clientDatabase.ClientInfo
import elite.kit.outwait.clientDatabase.ClientInfoDao
import elite.kit.outwait.remoteDataSource.ClientHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepository @Inject constructor(private val dao: ClientInfoDao, private val remote: ClientHandler) {

    private val activeSlots = MutableLiveData<List<ClientInfo>>()
    private val errorNotifications = MutableLiveData<List<ClientErrors>>()

    suspend fun newCodeEntered(code : String?) {
        withContext(IO){
            var ccode = "abc"
            if (code !== null){
                ccode = code
            }
            Log.d("newCodeEntered::cRepo", "entered code: $ccode")
            if(remote.initCommunication()){
                remote.newCodeEntered(ccode)
            }
        }
    }
    fun refreshWaitingTime(code : String){

    }
    fun getActiveSlots() : LiveData<List<ClientInfo>> = activeSlots
    fun getErrorNotifications() = errorNotifications as LiveData<List<ClientErrors>>
}
