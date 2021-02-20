package elite.kit.outwait.remoteDataSource

import androidx.lifecycle.LiveData

interface ClientHandler {

    // Methoden um Verbindung zum Server aufzubauen/abzubauen
    // Es müssen Referenzen auf den Handler bzw. das remoteDataSourcePaket bzw den Socket
    // gezählt werden, erst wenn niemand bzw. nur einer eine Ref. hält, darf Komm. plattgemacht werden
    fun initCommunication(): Boolean

    fun endCommunication(): Boolean

    /*
 Getter für LiveData um Events zu signalisieren? Eingeloggt, Ausgeloggt usw.
 //TODO Was wird hier gebraucht? Am besten mit LiveData?
 //Wie im AndroidKurs muss man dann auch sicherstellen, dass onEventFinish() aufgerufen wird!!
  */

    fun newCodeEntered(slotCode: String)

    fun refreshWaitingTime(slotCode: String)

    fun getErrors() : LiveData<List<ClientServerErrors>>
}
