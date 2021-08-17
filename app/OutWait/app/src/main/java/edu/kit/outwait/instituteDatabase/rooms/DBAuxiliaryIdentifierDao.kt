package edu.kit.outwait.instituteDatabase.rooms

import androidx.room.*

@Dao
interface DBAuxiliaryIdentifierDao {

    /**
     * creates a new row in the table that contains the slot code and
     * the auxiliary identifier encapsulated by [aux]
     *
     * @param aux encapsulates a slot code and the corresponding auxiliary identifier
     */
    @Insert
    suspend fun insert(aux: DBAuxiliaryIdentifier)

    /**
     * changes the auxiliary identifier for a slot code that already exists in
     * the table
     *
     * @param aux encapsulates the slot code and the its new auxiliary identifier
     */
    @Update
    suspend fun update(aux: DBAuxiliaryIdentifier)

    /**
     * deletes the given slot code with its auxiliary identifier from the table
     *
     * @param aux has to contain the slot code which has to be deleted
     */
    @Delete
    suspend fun delete(aux: DBAuxiliaryIdentifier)

    /**
     * deletes all slot codes and their auxiliary identifiers from the table
     *
     */
    @Query("DELETE FROM aux_identifiers")
    suspend fun clearTable()

    /**
     * returns the row of the given slot code as an [DBAuxiliaryIdentifier] object
     *
     * @param slotCode the slot code of which you want to get the auxiliary identifier
     * @return [DBAuxiliaryIdentifier] with slot code and corresponding auxiliary identifier
     */
    @Query("Select * From aux_identifiers Where slotCode = :slotCode")
    suspend fun getAuxIdentifier(slotCode: String): DBAuxiliaryIdentifier?

    /**
     * returns all slot codes and their auxiliary identifiers from the table
     * in a list
     *
     * @return all slot codes and their auxiliary identifiers from the table
     * in a list
     */
    @Query("Select * From aux_identifiers")
    suspend fun getIdentifierList(): List<DBAuxiliaryIdentifier>
}
