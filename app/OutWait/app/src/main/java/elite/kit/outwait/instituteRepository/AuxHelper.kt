package elite.kit.outwait.instituteRepository

import elite.kit.outwait.customDataTypes.ReceivedList
import elite.kit.outwait.instituteDatabase.facade.InstituteDBFacade

class AuxHelper(private var db : InstituteDBFacade) {
    private var latestAux : String = ""
    fun newAux(aux: String){
        latestAux = aux
    }
    fun receivedList(receivedList: ReceivedList) {
        if (latestAux != "") {
            val slotsInDB = db.getAuxiliaryIdentifiers().keys
            val slotsInReceived = receivedList.order
            val newSlotCodes = slotsInReceived.subtract(slotsInDB)
            if (newSlotCodes.size == 1) {
                db.insertUpdateAux(
                    newSlotCodes.first(),
                    latestAux
                )
                latestAux = ""
            }
        }
    }
}
