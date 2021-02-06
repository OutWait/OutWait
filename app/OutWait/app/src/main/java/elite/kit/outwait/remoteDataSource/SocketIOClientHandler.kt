package elite.kit.outwait.remoteDataSource

import org.json.JSONObject

class SocketIOClientHandler : ClientHandler {

    override fun initCommunication(): Boolean {
        TODO("Not yet implemented")
    }

    override fun endCommunication(): Boolean {
        TODO("Not yet implemented")
    }

    override fun newCodeEntered(code: String) {
        TODO("Not yet implemented")
    }

    override fun refreshWaitingTime(code: String) {
        TODO("Not yet implemented")
    }

    private fun processIncomingEvent(event: String, data: JSONObject) {

        //TODO Strategie verwenden um Daten zu verarbeiten

    }

}
