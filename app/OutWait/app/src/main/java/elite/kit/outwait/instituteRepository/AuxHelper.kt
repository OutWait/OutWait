package elite.kit.outwait.instituteRepository

import elite.kit.outwait.customDataTypes.ReceivedList
import elite.kit.outwait.instituteDatabase.facade.InstituteDBFacade

class AuxHelper(private var db: InstituteDBFacade) {
    private var latestAux = ""

    fun newAux(aux: String) {
        latestAux = aux
    }

    suspend fun receivedList(receivedList: ReceivedList, inTransaction: Boolean): Map<String, String> {
        val slotsInDB = db.getAuxiliaryIdentifiers().keys
        val slotsInReceived = receivedList.order
        val newSlotCodes = slotsInReceived.subtract(slotsInDB)

        if (newSlotCodes.isNotEmpty()) {
            for (code in newSlotCodes) {
                db.insertUpdateAux(code, latestAux)
            }
            latestAux = ""
        }

        if(!inTransaction){
            val obsoleteSlotCodes = slotsInDB.subtract(slotsInReceived)
            for (code in obsoleteSlotCodes){
                db.deleteAux(code)
            }
        }

        return db.getAuxiliaryIdentifiers()
    }


}
