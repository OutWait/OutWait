package elite.kit.outwait.instituteDatabase.rooms

import androidx.room.*

@Dao
interface DBAuxiliaryIdentifierDao {
    @Insert
    suspend fun insert(aux: DBAuxiliaryIdentifier)

    @Update
    suspend fun update(aux: DBAuxiliaryIdentifier)

    @Delete
    suspend fun delete(aux: DBAuxiliaryIdentifier)

    @Query("DELETE FROM aux_identifiers")
    suspend fun clearTable()

    @Query("Select * From aux_identifiers Where slotCode = :slotCode")
    suspend fun getAuxIdentifier(slotCode: String): DBAuxiliaryIdentifier?

    @Query("Select * From aux_identifiers")
    suspend fun getIdentifierList(): List<DBAuxiliaryIdentifier>
}
