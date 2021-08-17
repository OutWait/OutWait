package edu.kit.outwait.clientDatabase

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
/**
 * Interface to access the client_table of the client_db
 *
 */
interface ClientInfoDao {

    /**
     * Inserts the given ClientInfo object in the client_table
     *
     * @param clientInfo see [ClientInfo]
     */
    @Insert
    fun insert(clientInfo: ClientInfo)

    /**
     * Updates the ClientInfo object in the client_table with the same slot code
     *
     * @param clientInfo see [ClientInfo]
     */
    @Update
    fun update(clientInfo: ClientInfo)

    /**
     * looks up the clientInfo object with the given slot code (= wait code)
     *
     * @param waitCode see [ClientInfo.slotCode]
     * @return clientInfo with given slot Code or null if such a clientInfo does not exist
     */
    @Query("Select * From client_table Where slotCode = :waitCode")
    fun getClientInfo(waitCode: String): ClientInfo?

    /**
     * Delete the client info object with the same slot code from the client_table
     *
     * @param info the client info object
     */
    @Delete
    fun deleteClientInfo(info: ClientInfo)

    /**
     * like [getClientInfo], but the ClientInfo object is LiveData that informs its
     * observers when it changes.
     *
     * @param slotCode see [ClientInfo.slotCode]
     * @return clientInfo with given slot Code or null if such a clientInfo does not exist,
     * but encapsulated in a LiveData object (which is never null)
     */
    @Query("Select * From client_table Where slotCode = :slotCode")
    fun getClientInfoObservable(slotCode: String): LiveData<ClientInfo?>

    /**
     * get all stored clientInfo objects in a list
     *
     * @return all stored clientInfo objects in a list
     */
    @Query("Select * From client_table")
    fun getAllClientInfo(): List<ClientInfo>

    /**
     * like [getAllClientInfo], but the list is LiveData that informs its
     * observers when it changes.
     *
     * @return all stored clientInfo objects in a LiveData list
     */
    @Query("Select * From client_table")
    fun getAllClientInfoObservable(): LiveData<List<ClientInfo>>

    /**
     * Deletes everything in the client_table
     *
     */
    @Query("DELETE FROM client_table")
    fun clearTable()
}
