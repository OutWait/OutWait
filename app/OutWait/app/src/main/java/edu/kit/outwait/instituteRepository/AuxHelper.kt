package edu.kit.outwait.instituteRepository

import edu.kit.outwait.customDataTypes.ReceivedList
import edu.kit.outwait.instituteDatabase.facade.InstituteDBFacade

/**
 * Helper class that simplifies the complex work of storing auxiliary identifiers
 * for client slots. The problem is that when the receptionist enters the auxiliary
 * identifier for a new slot, the slot code is not known yet. Hence one has to remember
 * the entered aux until the server is sending the actualized queue, and then
 * figure out which is the new slot code in the list and then write this slot code
 * with the aux to the database.
 *
 * @property db database facade so that this helper class can write directly to the
 * database
 */
internal class AuxHelper(private var db: InstituteDBFacade) {
    private var latestAux = ""

    /**
     * Remember the auxiliary identifier until the next queue is received
     *
     * @param aux auxiliary identifier entered by the receptionist
     */
    fun newAux(aux: String) {
        latestAux = aux
    }

    /**
     * Figures out the new slot and writes its slot code with the latest saved
     * aux to the db.
     * Deletes obsolete auxiliary identifiers.
     *
     * @param receivedList the latest received waiting queue from the server
     * @param inTransaction if a transaction is running obsolete auxiliary identifiers
     * are not deleted, because their slots can recur if the transaction is aborted
     * @return actualized map with that has the slot codes of the inserted slots as keys
     * and their auxiliary identifiers as values
     */
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

    suspend fun changeAux(slotCode: String, newAux: String){
        db.insertUpdateAux(slotCode, newAux)
    }
}
