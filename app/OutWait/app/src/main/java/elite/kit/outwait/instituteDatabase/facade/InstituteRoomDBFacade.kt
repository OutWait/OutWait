package elite.kit.outwait.instituteDatabase.facade

import elite.kit.outwait.instituteDatabase.rooms.DBAuxiliaryIdentifier
import elite.kit.outwait.instituteDatabase.rooms.DBAuxiliaryIdentifierDao
import javax.inject.Inject

class InstituteRoomDBFacade @Inject constructor(
    private val dao: DBAuxiliaryIdentifierDao
    ) : InstituteDBFacade {

    //insert or update!
    override fun insertUpdateAux(slotCode: String, aux: String) {
        val auxToInsert = DBAuxiliaryIdentifier(slotCode, aux)
        if(dao.getAuxIdentifier(slotCode) == null){
            dao.insert(auxToInsert)
        } else{
            dao.update(auxToInsert)
        }
    }

    override fun getAuxiliaryIdentifiers(): Map<String, String> {
        val auxObjects = dao.getIdentifierList()
        return auxObjects.associate {
            Pair(it.slotCode, it.auxiliaryText)
        }
    }

    override fun deleteAll() {
        dao.clearTable()
    }
}
