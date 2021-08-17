package edu.kit.outwait.instituteRepository

import edu.kit.outwait.instituteDatabase.facade.InstituteDBFacade

/**
 * Simulates the Behavior of the real database, only without persistance
 *
 */
class DataBaseFake : InstituteDBFacade {

    private val auxMap: MutableMap<String, String> = HashMap<String, String>()
    private var loginData: Pair<String, String>? = null

    override suspend fun insertUpdateAux(slotCode: String, aux: String) {
        auxMap[slotCode] = aux
    }

    override suspend fun getAuxiliaryIdentifiers(): Map<String, String> {
        return auxMap.toMap()
    }

    override suspend fun deleteAux(slotCode: String) {
        auxMap.remove(slotCode)
    }

    override suspend fun deleteAll() {
        auxMap.clear()
        loginData = null
    }

    override suspend fun insertUpdateLoginData(username: String, password: String) {
        loginData = Pair(username, password)
    }

    override suspend fun getUserName(): String
        =   if (loginData !== null) loginData!!.first
            else ""

    override suspend fun getPassword(): String
        =   if (loginData !== null) loginData!!.second
            else ""

    override suspend fun loginDataSaved(): Boolean
        =   loginData !== null
}
