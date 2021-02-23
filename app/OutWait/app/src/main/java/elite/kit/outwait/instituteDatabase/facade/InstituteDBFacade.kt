package elite.kit.outwait.instituteDatabase.facade

interface InstituteDBFacade {
    fun insertAuxiliaryIdentifier(slotCode: String, aux: String)
    fun getAuxiliaryIdentifiers() : HashMap<String, String>
    fun deleteAll()
}
