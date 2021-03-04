package elite.kit.outwait.instituteDatabase.facade

import elite.kit.outwait.instituteDatabase.rooms.DBAuxiliaryIdentifier
import elite.kit.outwait.instituteDatabase.rooms.DBAuxiliaryIdentifierDao
import javax.inject.Inject

/**
 * Implements the InstituteDBFacade for our room database
 *
 * @property dao data access object for the auxiliary identifier table
 */
class InstituteRoomDBFacade @Inject constructor(
    private val dao: DBAuxiliaryIdentifierDao
    ) : InstituteDBFacade {

    override suspend fun insertUpdateAux(slotCode: String, aux: String) {
        val auxToInsert = DBAuxiliaryIdentifier(slotCode, aux)
        if(dao.getAuxIdentifier(slotCode) == null){
            dao.insert(auxToInsert)
        } else{
            dao.update(auxToInsert)
        }
    }

    override suspend fun getAuxiliaryIdentifiers(): Map<String, String> {
        val auxObjects = dao.getIdentifierList()
        return auxObjects.associate {
            Pair(it.slotCode, it.auxiliaryText)
        }
    }

    override suspend fun deleteAux(slotCode: String) {
        dao.delete(DBAuxiliaryIdentifier(slotCode, ""))
    }

    override suspend fun deleteAll() {
        dao.clearTable()
    }
}
