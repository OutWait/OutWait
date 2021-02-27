package elite.kit.outwait.instituteDatabase.rooms

import androidx.room.*

@Dao
interface DBAuxiliaryIdentifierDao {
    @Insert
    fun insert(aux: DBAuxiliaryIdentifier)

    @Update
    fun update(aux: DBAuxiliaryIdentifier)

    @Delete
    fun delete(aux: DBAuxiliaryIdentifier)

    @Query("DELETE FROM aux_identifiers")
    fun clearTable()

    @Query("Select * From aux_identifiers Where :slotCode")
    fun getAuxIdentifier(slotCode: String): DBAuxiliaryIdentifier?

    @Query("Select * From aux_identifiers")
    fun getIdentifierList(): List<DBAuxiliaryIdentifier>
}
