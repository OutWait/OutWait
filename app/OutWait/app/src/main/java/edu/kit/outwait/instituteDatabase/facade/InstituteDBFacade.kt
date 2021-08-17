package edu.kit.outwait.instituteDatabase.facade

/**
 * Encapsulates the institute database where auxiliary identifiers and login data are saved
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

    /**
     * Inserts new login Data to the database (username + password). If there
     * are already saved other login data, it will be overwritten
     *
     * @param username the username of the institution
     * @param password the password of the institution
     */
    suspend fun insertUpdateLoginData(username: String, password: String)

    /**
     * Returns the username
     *
     * @return username or empty string if no username is saved
     */
    suspend fun getUserName(): String

    /**
     * Returns the password
     *
     * @return password or empty string if no password is saved
     */
    suspend fun getPassword(): String

    /**
     * Checks if there is saved login data in the database
     *
     * @return true if there is saved login data and false if not.
     */
    suspend fun loginDataSaved(): Boolean
}
