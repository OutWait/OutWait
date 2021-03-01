package elite.kit.outwait.instituteDatabase.facade

interface InstituteDBFacade {
    suspend fun insertUpdateAux(slotCode: String, aux: String)
    suspend fun getAuxiliaryIdentifiers() : Map<String, String>
    suspend fun deleteAux(slotCode: String)
    suspend fun deleteAll()
}
