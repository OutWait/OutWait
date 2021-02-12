package elite.kit.outwait.clientRepository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elite.kit.outwait.clientDatabase.ClientInfo
import elite.kit.outwait.clientDatabase.ClientInfoDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepository @Inject constructor(private val dao: ClientInfoDao) {

    private val activeSlots = MutableLiveData<List<ClientInfo>>()
    private val errorNotifications = MutableLiveData<List<String>>()

    fun newCodeEntered(code : String){

    }
    fun refreshWaitingTime(code : String){

    }
    fun getActiveSlots() : LiveData<List<ClientInfo>> = activeSlots
    fun getErrorNotifications() : LiveData<List<String>> = errorNotifications
}
