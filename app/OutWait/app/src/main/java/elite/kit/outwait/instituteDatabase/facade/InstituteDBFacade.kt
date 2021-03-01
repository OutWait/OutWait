package elite.kit.outwait.instituteDatabase.facade

interface InstituteDBFacade {
    fun insertUpdateAux(slotCode: String, aux: String)
    fun getAuxiliaryIdentifiers() : Map<String, String>
    fun deleteAux(slotCode: String)
    fun deleteAll()
}
