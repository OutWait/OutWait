package elite.kit.outwait.clientDatabase

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ClientInfoDao {

    @Insert
    fun insert(clientInfo: ClientInfo)

    @Update
    fun update(clientInfo: ClientInfo)

    @Query("Select * From client_table Where :waitCode")
    fun getClientInfo(waitCode: String): ClientInfo

    @Delete
    fun deleteClientInfo(info: ClientInfo)

    @Query("Select * From client_table Where :slotCode")
    fun getClientInfoObservable(slotCode: String): LiveData<ClientInfo>

    @Query("Select * From client_table")
    fun getAllClientInfo(): List<ClientInfo>

    @Query("Select * From client_table")
    fun getAllClientInfoObservable(): LiveData<List<ClientInfo>>

    @Query("DELETE FROM client_table")
    fun clearTable()
}
