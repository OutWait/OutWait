package elite.kit.outwait.instituteDatabase.facade

/**
 * Encapsulates the institute database where the auxiliary identifiers are saved
 *
 */
interface InstituteDBFacade {
    /**
     * inserts a new auxiliary identifier text for the given slot code in the
     * table. In case the slot code already exists, the auxiliary identifier
     * test is updated
     *
     * @param slotCode code of the slot for which we want to store the auxiliary identifier
     * @param aux the auxiliary identifier text entered by the staff
     */
    suspend fun insertUpdateAux(slotCode: String, aux: String)

    /**
     * returns a map with that has the slot codes of the inserted slots as
     * keys and their auxiliary identifiers as values
     *
     * @return map as described in method description
     */
    suspend fun getAuxiliaryIdentifiers() : Map<String, String>

    /**
     * deletes the given slot code with its auxiliary identifier from
     * the table
     *
     * @param slotCode
     */
    suspend fun deleteAux(slotCode: String)

    /**
     * deletes all slot codes and their auxiliary identifiers from the table
     *
     */
    suspend fun deleteAll()
}
