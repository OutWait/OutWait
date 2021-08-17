package edu.kit.outwait.instituteDatabase.facade

import edu.kit.outwait.instituteDatabase.rooms.DBAuxiliaryIdentifier
import edu.kit.outwait.instituteDatabase.rooms.DBAuxiliaryIdentifierDao
import edu.kit.outwait.instituteDatabase.rooms.DBLoginData
import edu.kit.outwait.instituteDatabase.rooms.DBLoginDataDao
import javax.inject.Inject

/**
 * Implements the InstituteDBFacade for our room database
 *
 * @property dao data access object for the auxiliary identifier table
 */
class InstituteRoomDBFacade @Inject constructor(
    private val auxDao: DBAuxiliaryIdentifierDao,
    private val loginDao: DBLoginDataDao
    ) : InstituteDBFacade {

    override suspend fun insertUpdateAux(slotCode: String, aux: String) {
        val auxToInsert = DBAuxiliaryIdentifier(slotCode, aux)
        if(auxDao.getAuxIdentifier(slotCode) == null){
            auxDao.insert(auxToInsert)
        } else{
            auxDao.update(auxToInsert)
        }
    }

    override suspend fun getAuxiliaryIdentifiers(): Map<String, String> {
        val auxObjects = auxDao.getIdentifierList()
        return auxObjects.associate {
            Pair(it.slotCode, it.auxiliaryText)
        }
    }

    override suspend fun deleteAux(slotCode: String) {
        auxDao.delete(DBAuxiliaryIdentifier(slotCode, ""))
    }

    override suspend fun deleteAll() {
        auxDao.clearTable()
        loginDao.clearTable()
    }

    override suspend fun insertUpdateLoginData(username: String, password: String) {
        loginDao.clearTable()
        loginDao.insert(
            DBLoginData(username, password)
        )
    }

    override suspend fun getUserName(): String
        = if (loginDataSaved()){
            loginDao.getAllLoginData().first().username
        } else {
            ""
        }

    override suspend fun getPassword(): String
        = if (loginDataSaved()){
            loginDao.getAllLoginData().first().password
        } else {
            ""
        }

    override suspend fun loginDataSaved(): Boolean
        = loginDao.getAllLoginData().isNotEmpty()

}
